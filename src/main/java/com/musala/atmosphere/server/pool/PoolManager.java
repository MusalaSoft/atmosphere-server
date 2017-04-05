package com.musala.atmosphere.server.pool;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.IClientBuilder;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.cs.exception.DeviceNotFoundException;
import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.PasskeyAuthority;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;
import com.musala.atmosphere.server.registrymanager.RemoteObjectRegistryManager;

/**
 * Class which is responsible for iterating with the devices in the pool. It also sending events to
 * {@link RemoteObjectRegistryManager} when e device is add to the pool or removed.
 *
 * @author yavor.stankov
 *
 */
public class PoolManager extends UnicastRemoteObject implements IClientBuilder, Subscriber {
    private static final long serialVersionUID = -5077918124351182199L;

    private static final String DEVICE_RMI_ID_FORMAT = "%s_%s";

    private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

    private static PoolManager poolManagerInstance = null;

    private ServerEventService eventService = new ServerEventService();

    private IDevicePoolDao devicePoolDao;

    private HashMap<String, DeviceProxy> deviceIdToDeviceProxy = new HashMap<>();

    private PoolManager() throws RemoteException {
    }

    /**
     * Creates an instance (or gets the current if such instance already exists) of the {@link PoolManager PoolManager}.
     *
     * @return PoolManager Instance
     */
    public static PoolManager getInstance() {
        if (poolManagerInstance == null) {
            synchronized (PoolManager.class) {
                if (poolManagerInstance == null) {
                    try {
                        poolManagerInstance = new PoolManager();
                        return poolManagerInstance;
                    } catch (RemoteException e) {
                        // This is never thrown really, it's just that UnicastRemoteObject requires the constructor to
                        // throw a RemoteException.
                        LOGGER.fatal("Instance of PoolManager could not be retrieved.", e);
                        return null;
                    }
                }
            }
        }

        return poolManagerInstance;
    }

    /**
     * Refreshes the current state of the device - removes the device from the pool if it is not present on an Agent
     * anymore and remove the device from the server's RMI registry.
     *
     * @param deviceId
     *        - the device ID
     * @throws CommandFailedException
     *         - if failed to get device information, because of shell exception
     *
     * @throws RemoteException
     *         - if failed to get the device information from the DeviceProxy
     * @throws DevicePoolDaoException
     *         - if failed to release device with the given ID
     */
    public void removeDevice(String deviceId) throws RemoteException, CommandFailedException, DevicePoolDaoException {
        DeviceProxy deviceProxy = deviceIdToDeviceProxy.get(deviceId);

        IDevice device = devicePoolDao.getDevice(deviceId);
        DeviceInformation deviceInformation = device.getInformation();
        String deviceSerialNumber = deviceInformation.getSerialNumber();
        String agentId = device.getAgentId();

        DevicePublishEvent event = new DeviceUnpublishedEvent(deviceProxy, deviceSerialNumber, agentId);
        eventService.publish(event);

        devicePoolDao.remove(deviceId);
        deviceIdToDeviceProxy.remove(deviceId);

        LOGGER.info("Device with id " + deviceId + " disconnected and removed.");
    }

    /**
     * Adds a device to the pool.
     *
     * @param deviceRmiId
     *        - RMI string identifier for the device wrapper stub on the Agent registry
     * @param agentRegistry
     *        - the Agent {@link Registry} object that contains the device that will be added
     * @param agentId
     *        - the ID of the agent
     * @return the ID of the device in the pool if it was successfully inserted, or <code>null</code> otherwise
     *
     */
    public String addDevice(String deviceRmiId, Registry agentRegistry, String agentId) {
        try {
            IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(deviceRmiId);

            DeviceInformation deviceInformation = (DeviceInformation) deviceWrapper.route(RoutingAction.GET_DEVICE_INFORMATION);

            String deviceSerialNumber = deviceInformation.getSerialNumber();

            String deviceId = buildDeviceIdentifier(agentId, deviceSerialNumber);

            DeviceProxy deviceProxy = new DeviceProxy(deviceWrapper, deviceId);

            DevicePublishEvent event = new DevicePublishedEvent(deviceProxy, deviceSerialNumber, agentId);
            eventService.publish(event);

            deviceIdToDeviceProxy.put(deviceId, deviceProxy);

            long devicePasskey = PasskeyAuthority.generatePasskey();

            try {
                devicePoolDao.addDevice(deviceInformation, deviceId, agentId, devicePasskey);
            } catch (DevicePoolDaoException e) {
                String errorMessage = String.format("Failed to add device with ID %s on agent %s.", deviceId, agentId);
                LOGGER.error(errorMessage);
            }

            return deviceId;
        } catch (NotBoundException e) {
            LOGGER.warn("Attempted to get a non-bound device wrapper [" + deviceRmiId + "] from an Agent.", e);
        } catch (RemoteException e) {
            LOGGER.warn("Attempted to get a device wrapper from an Agent that we can not connect to.", e);
        } catch (CommandFailedException e) {
            LOGGER.error("Attemted to add a device that fails to supply describing information.", e);
        }

        return null;
    }

    /**
     * Remove all devices from the pool.
     *
     * @throws CommandFailedException
     *         - when executing command on a connected device fails
     * @throws RemoteException
     *         - thrown when an error during the execution of a remote method call.
     * @throws DevicePoolDaoException
     *         - this exception is thrown when operations with the data access object for the device pool in the data
     *         source fail.
     */
    public void removeAllDevices() throws RemoteException, CommandFailedException, DevicePoolDaoException {
        List<String> devicesToRemove = new ArrayList<>(deviceIdToDeviceProxy.keySet());
        for (String deviceId : devicesToRemove) {
            removeDevice(deviceId);
        }
    }

    @Override
    public void releaseDevice(DeviceAllocationInformation allocatedDeviceDescriptor)
        throws RemoteException,
            InvalidPasskeyException,
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

    @Override
    public synchronized DeviceAllocationInformation allocateDevice(DeviceSelector deviceSelector) {
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
        String deviceSerialNumber = deviceInformation.getSerialNumber();
        String onAgentId = device.getAgentId();

        String bestMatchDeviceRmiId = buildDeviceIdentifier(onAgentId, deviceSerialNumber);

        device.allocate();

        try {
            devicePoolDao.update(device);
        } catch (DevicePoolDaoException e) {
            String message = String.format("Allocating device with serial number %s failed.",
                                           deviceInformation.getSerialNumber());
            LOGGER.error(message, e);
        }

        String deviceId = device.getDeviceId();

        long devicePasskey = device.getPasskey();

        DeviceAllocationInformation allocatedDeviceDescriptor = new DeviceAllocationInformation(bestMatchDeviceRmiId,
                                                                                                devicePasskey,
                                                                                                deviceId);
        ClientRequestMonitor deviceMonitor = new ClientRequestMonitor();
        deviceMonitor.restartTimerForDevice(bestMatchDeviceRmiId);

        return allocatedDeviceDescriptor;
    }

    @Override
    public synchronized List<Pair<String,String>> getAllAvailableDevices() throws RemoteException{
    	List<IDevice> availableDevicesList = new ArrayList<>();

        boolean isAllocated = false;

        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().minApi(17);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();

        try {
            availableDevicesList = devicePoolDao.getDevices(deviceSelector, isAllocated);

            if (availableDevicesList.isEmpty()) {
            	return new ArrayList<Pair<String,String>>();
            }
        } catch (DevicePoolDaoException e) {
            throw new NoDeviceMatchingTheGivenSelectorException();
        }

        ArrayList<Pair<String,String>> serialNumberAndModelList = new ArrayList<Pair<String,String>>();

        for(int index = 0; index < availableDevicesList.size(); index++) {
        	IDevice device = availableDevicesList.get(index);
        	DeviceInformation deviceInformation = device.getInformation();

        	serialNumberAndModelList.add(new Pair<String, String>(deviceInformation.getSerialNumber(), deviceInformation.getModel()));
        }

        return serialNumberAndModelList;
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
     * Updates the information of the device.
     *
     * @param deviceRmiId
     *        - RMI string identifier for the device wrapper stub on the Agent {@link Registry}
     * @param deviceId
     *        - unique identifier of the device
     * @param agentRegistry
     *        - the Agent {@link Registry} object that contains the device's changed information
     */
    public void updateDevice(String deviceRmiId, String deviceId, Registry agentRegistry) {
        String agentId = null;
        try {
            IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(deviceRmiId);
            DeviceInformation deviceInformation = (DeviceInformation) deviceWrapper.route(RoutingAction.GET_DEVICE_INFORMATION);

            IDevice deviceToUpdate = devicePoolDao.getDevice(deviceId);
            agentId = deviceToUpdate.getAgentId();
            deviceToUpdate.setDeviceInformation(deviceInformation);
            devicePoolDao.update(deviceToUpdate);

        } catch (AccessException e) {
            String accessExceptionMessage = String.format("No permission to perfrom the requested update of the device with id %s.",
                                                          deviceId);
            LOGGER.debug(accessExceptionMessage, e);
        } catch (RemoteException e) {
            String remoteExceptionMessage = String.format("Connection to the Agent with id [%s] has been lost.",
                                                          agentId);
            LOGGER.warn(remoteExceptionMessage, e);
        } catch (NotBoundException e) {
            String notBoundExceptionMessage = String.format("Attempted to get a non-bound device wrapper [%s] from an Agent with id [%s].",
                                                            deviceId,
                                                            agentId);
            LOGGER.warn(notBoundExceptionMessage, e);
        } catch (CommandFailedException e) {
            String commandFailedExceptionMessage = String.format("Getting the new device information from the Agent with id [%s] failed.",
                                                                 agentId);
            LOGGER.warn(commandFailedExceptionMessage, e);
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

    private static String buildDeviceIdentifier(String onAgentId, String deviceSerialNumber) {
        String deviceIdentifier = String.format(DEVICE_RMI_ID_FORMAT, onAgentId, deviceSerialNumber);

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
    }
}
