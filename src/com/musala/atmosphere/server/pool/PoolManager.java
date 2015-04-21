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
import com.musala.atmosphere.commons.cs.exception.DeviceNotFoundException;
import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.NoAvailableDeviceFoundException;
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

    private HashMap<String, DeviceProxy> deviceIdToDeviceProxy = new HashMap<String, DeviceProxy>();

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
     * @param agentManager
     *        - the {@link AgentManager AgentManager} that published the {@link IWrapDevice IWrapDevice} we are wrapping
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
     * @throws RemoteException
     * @throws DevicePoolDaoException
     */
    public void removeAllDevices() throws RemoteException, CommandFailedException, DevicePoolDaoException {
        List<String> devicesToRemove = new ArrayList<String>(deviceIdToDeviceProxy.keySet());
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

        long passkey = PasskeyAuthority.generatePasskey(currentPasskey);

        try {
            IDevice device = devicePoolDao.getDevice(deviceId);
            device.setPasskey(passkey);
            devicePoolDao.update(device);
        } catch (DevicePoolDaoException e) {
            String errorMessage = String.format("Failed to reset the passkey of a device.");
            throw new DeviceNotFoundException(errorMessage);
        }

        try {
            releaseDevice(deviceId);
        } catch (DevicePoolDaoException e) {
            String errorMessage = String.format("Failed to release a device.");
            throw new DeviceNotFoundException(errorMessage);
        }
    }

    public void releaseDevice(String deviceId) throws RemoteException, DevicePoolDaoException {

        IDevice device = devicePoolDao.getDevice(deviceId);

        if (device != null) {
            device.release();
            devicePoolDao.update(device);
        }
    }

    @Override
    public synchronized DeviceAllocationInformation allocateDevice(DeviceSelector deviceSelector)
        throws RemoteException {
        List<IDevice> deviceList = new ArrayList<IDevice>();
        String errorMessage = String.format("No devices matching the requested parameters %s were found",
                                            deviceSelector);
        boolean isAllocated = false;

        try {
            deviceList = devicePoolDao.getDevices(deviceSelector, isAllocated);
        } catch (DevicePoolDaoException e) {
            throw new NoAvailableDeviceFoundException(errorMessage, e);
        }

        if (deviceList.isEmpty()) {
            throw new NoAvailableDeviceFoundException(errorMessage);
        }

        IDevice device = deviceList.get(0);

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

    public void inform(DevicePoolDaoCreatedEvent event) {
        IDataSourceProvider dataSoureceProvider = new DataSourceProvider();
        devicePoolDao = dataSoureceProvider.getDevicePoolDao();
    }

    private static String buildDeviceIdentifier(String onAgentId, String deviceSerialNumber) {
        String deviceIdentifier = String.format(DEVICE_RMI_ID_FORMAT, onAgentId, deviceSerialNumber);

        return deviceIdentifier;
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
}
