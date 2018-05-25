// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.server.pool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.cs.exception.DeviceNotFoundException;
import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.PasskeyAuthority;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.device.allocate.DeviceReleasedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Class which is responsible for iterating with the devices in the pool.
 *
 * @author yavor.stankov
 *
 */
public class PoolManager implements Subscriber {
    static final String DEVICE_ID_FORMAT = "%s_%s";

    private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

    private ServerEventService eventService = new ServerEventService();

    private IDevicePoolDao devicePoolDao;

    private Set<String> deviceIdsCache = Collections.synchronizedSet(new HashSet<String>());

    private static class PoolManagerLoader {
        private static final PoolManager INSTANCE = new PoolManager();
    }

    /**
     * Creates an instance (or gets the current if such instance already exists) of the {@link PoolManager PoolManager}.
     *
     * @return PoolManager Instance
     */
    public static PoolManager getInstance() {
        return PoolManagerLoader.INSTANCE;
    }

    /**
     * Refreshes the current state of the device - removes the device from the pool if it is not present on an Agent
     * anymore and remove the device id from the cache.
     *
     * @param deviceId
     *        - the device ID
     * @throws CommandFailedException
     *         - if failed to get device information, because of shell exception
     * @throws DevicePoolDaoException
     *         - if failed to release device with the given ID
     */
    public void removeDevice(String deviceId) throws DevicePoolDaoException, CommandFailedException {
        removeDevice(deviceId, true);
    }

    private void removeDevice(String deviceId, boolean removeFromCache)
        throws CommandFailedException,
            DevicePoolDaoException {
        IDevice device = devicePoolDao.getDevice(deviceId);
        if (device != null) {
            DeviceInformation deviceInformation = device.getInformation();
            String deviceSerialNumber = deviceInformation.getSerialNumber();
            String agentId = device.getAgentId();

            DevicePublishEvent event = new DeviceUnpublishedEvent(deviceSerialNumber, agentId);
            eventService.publish(event);

            devicePoolDao.remove(deviceId);
            if (removeFromCache) {
                deviceIdsCache.remove(deviceId);
            }
            LOGGER.info("Device with id " + deviceId + " disconnected and removed.");
        }
    }

    /**
     * Adds a device to the pool.
     *
     * @param deviceInformation
     *        - an information about the device
     * @param agentId
     *        - the ID of the agent on which the device is attached
     * @return the ID of the device in the pool if it was successfully inserted, or <code>null</code> otherwise
     *
     */
    public String addDevice(DeviceInformation deviceInformation, String agentId) {
        String deviceSerialNumber = deviceInformation.getSerialNumber();
        String deviceId = buildDeviceIdentifier(agentId, deviceSerialNumber);

        deviceIdsCache.add(deviceId);
        long devicePasskey = PasskeyAuthority.generatePasskey();

        try {
            devicePoolDao.addDevice(deviceInformation, deviceId, agentId, devicePasskey);
            DevicePublishEvent event = new DevicePublishedEvent(agentId, deviceInformation);
            eventService.publish(event);

            LOGGER.info("Device with serialNumber = " + deviceSerialNumber + " added to the pool.");
        } catch (DevicePoolDaoException e) {
            String errorMessage = String.format("Failed to add device with ID %s on agent %s.", deviceId, agentId);
            LOGGER.error(errorMessage);

            return null;
        }

        return deviceId;
    }

    /**
     * Remove all devices from the pool.
     *
     * @throws CommandFailedException
     *         - when executing command on a connected device fails
     * @throws DevicePoolDaoException
     *         - this exception is thrown when operations with the data access object for the device pool in the data
     *         source fail.
     */
    public void removeAllDevices() throws CommandFailedException, DevicePoolDaoException {
        // uses an iterator to prevent the ConcurrentModificationException when trying to remove an item from the cache
        Iterator<String> deviceCacheIterator = deviceIdsCache.iterator();
        while (deviceCacheIterator.hasNext()) {
            removeDevice(deviceCacheIterator.next(), false);
            deviceCacheIterator.remove();
        }
    }

    public void releaseDevice(DeviceAllocationInformation allocatedDeviceDescriptor)
        throws InvalidPasskeyException,
            DeviceNotFoundException {
        String deviceId = allocatedDeviceDescriptor.getDeviceId();
        long currentPasskey = allocatedDeviceDescriptor.getProxyPasskey();

        PasskeyAuthority.validatePasskey(currentPasskey, deviceId);

        try {
            IDevice device = devicePoolDao.getDevice(deviceId);

            if (device != null) {
                releaseDevice(device, currentPasskey);
            }
        } catch (DevicePoolDaoException e) {
            String errorMessage = String.format("Failed to release device with ID %s.", deviceId);
            LOGGER.fatal(errorMessage);
        }
    }

    public synchronized DeviceAllocationInformation allocateDevice(DeviceSelector deviceSelector)
        throws NoDeviceMatchingTheGivenSelectorException,
            NoAvailableDeviceFoundException {
        List<IDevice> availableDevicesList = new ArrayList<>();
        String errorMessage = String.format("No devices matching the requested parameters %s were found",
                                            deviceSelector);
        boolean isAllocated = false;

        try {
            availableDevicesList = devicePoolDao.getDevices(deviceSelector, isAllocated);
            if (availableDevicesList.isEmpty()) {
                List<IDevice> notAvailableDeviceList = devicePoolDao.getDevices(deviceSelector, !isAllocated);
                if (notAvailableDeviceList.isEmpty()) {
                    throw new NoDeviceMatchingTheGivenSelectorException();
                } else {
                    throw new NoAvailableDeviceFoundException(errorMessage);
                }
            }
        } catch (DevicePoolDaoException e) {
            throw new NoDeviceMatchingTheGivenSelectorException();
        }

        IDevice device = availableDevicesList.get(0);

        DeviceInformation deviceInformation = device.getInformation();

        device.allocate();

        try {
            devicePoolDao.update(device);
        } catch (DevicePoolDaoException e) {
            String message = String.format("Allocating device with serial number %s failed.",
                                           deviceInformation.getSerialNumber());
            LOGGER.error(message, e);
        }

        final String bestMatchDeviceId = device.getDeviceId();

        long devicePasskey = device.getPasskey();

        DeviceAllocationInformation allocatedDeviceDescriptor = new DeviceAllocationInformation(devicePasskey,
                                                                                                bestMatchDeviceId);
        ClientRequestMonitor deviceMonitor = new ClientRequestMonitor();
        deviceMonitor.restartTimerForDevice(bestMatchDeviceId);

        return allocatedDeviceDescriptor;
    }

    /**
     * Gets a list with serial numbers and models of all available devices.
     *
     * @return a {@List list} with {@Pair<Strin, String> serial numbers and models} of available devices
     */
    public synchronized List<Pair<String, String>> getAllAvailableDevices() {
        List<IDevice> availableDevicesList = getAllDevices(false);

        ArrayList<Pair<String, String>> serialNumberAndModelList = new ArrayList<>();

        for (IDevice device : availableDevicesList) {
            DeviceInformation deviceInformation = device.getInformation();
            serialNumberAndModelList.add(new Pair<>(deviceInformation.getSerialNumber(), deviceInformation.getModel()));
        }

        return serialNumberAndModelList;
    }

    /**
     * Gets the IDs of the all allocated devices.
     *
     * @return {@List list} of IDs of the all allocated devices
     */
    public List<String> getAllAllocatedDevices() {
        List<IDevice> allocatedDeviceLlist = getAllDevices(true);

        ArrayList<String> allocatedDeviceIds = new ArrayList<>();

        for (IDevice device : allocatedDeviceLlist) {
            allocatedDeviceIds.add(device.getAgentId() + "_" + device.getInformation().getSerialNumber());
        }

        return allocatedDeviceIds;
    }

    /**
     * Returns a list of devices that meet a certain criteria(allocated or available).
     *
     * @param isAllocated
     *        - <code>true</code> to select the allocated devices, <code>false</code> to select the available devices
     * @return a {@List list} of {@IDevice devices}
     */
    private List<IDevice> getAllDevices(boolean isAllocated) {
        List<IDevice> deviceList = new ArrayList<>();
        DeviceSelector deviceSelector = new DeviceSelectorBuilder().minApi(17).build();

        try {
            deviceList = devicePoolDao.getDevices(deviceSelector, isAllocated);
        } catch (DevicePoolDaoException e) {
            throw new NoDeviceMatchingTheGivenSelectorException();
        }

        return deviceList;
    }

    /**
     * Releases allocated device by its ID and returns it in the pool.
     *
     * @param deviceId
     *        - unique identifier for device matching
     * @throws DevicePoolDaoException
     *         when the data source is not available
     */
    public void releaseDevice(String deviceId) throws DevicePoolDaoException {
        IDevice device = devicePoolDao.getDevice(deviceId);

        if (device != null) {
            releaseDevice(device, device.getPasskey());
        }
    }

    /**
     * TODO: This method will never be called. Consider to remove it or write a calling logic on the Agent.
     *
     * Updates the information of the device.
     *
     * @param deviceId
     *        - unique identifier of the device
     * @param deviceInformation
     *        - {@link DeviceInformation information} about the device
     */
    public void updateDevice(String deviceId, DeviceInformation deviceInformation) {
        try {
            IDevice deviceToUpdate = devicePoolDao.getDevice(deviceId);
            deviceToUpdate.setDeviceInformation(deviceInformation);
            devicePoolDao.update(deviceToUpdate);

        } catch (DevicePoolDaoException e) {
            String devicePoolDaoExceptionMessage = String.format("Updating of the device [%s] failed or it was not present in the database.",
                                                                 deviceId);
            LOGGER.warn(devicePoolDaoExceptionMessage, e);
        }
    }

    public void inform(DevicePoolDaoCreatedEvent event) {
        IDataSourceProvider dataSoureceProvider = new DataSourceProvider();
        devicePoolDao = dataSoureceProvider.getDevicePoolDao();
    }

    public static String buildDeviceIdentifier(String onAgentId, String deviceSerialNumber) {
        String deviceIdentifier = String.format(DEVICE_ID_FORMAT, onAgentId, deviceSerialNumber);

        return deviceIdentifier;
    }

    /**
     * Releases {@link IDevice device instance} and updates its passkey used for requests validation.
     *
     * @param device
     *        - {@link IDevice device instance} to be released
     * @param currentPasskey
     *        - currently used invocation passkey for this device
     * @throws DevicePoolDaoException
     *         when data source for device retrieving is not available or operations with data source fails
     */
    private void releaseDevice(IDevice device, long currentPasskey) throws DevicePoolDaoException {
        long passkey = PasskeyAuthority.generatePasskey(currentPasskey);

        device.setPasskey(passkey);
        device.release();

        devicePoolDao.update(device);
        eventService.publish(new DeviceReleasedEvent(device.getInformation()));
    }

    public IDevicePoolDao getDevicePoolDao() {
        return devicePoolDao;
    }

    /**
     * Gets a {@link IDevice device} by identifier
     *
     * @param deviceId
     *        - the identifier of the device
     * @return - {@link IDevice device}
     */
    public IDevice getDeviceById(String deviceId) {
        IDevice device = null;
        try {
            device = devicePoolDao.getDevice(deviceId);
        } catch (DevicePoolDaoException e) {
            String errorMessage = String.format("Failed to get a device with ID %s .", deviceId);
            LOGGER.error(errorMessage);
        }

        return device;
    }
}
