package com.musala.atmosphere.server.pool;

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
import com.musala.atmosphere.commons.cs.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.IClientBuilder;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.PasskeyAuthority;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.dao.nativeobject.DevicePoolDao;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.registrymanager.RemoteObjectRegistryManager;

/**
 * Class which is responsible for iterating with the devices in the pool. It also sending events to
 * {@link RemoteObjectRegistryManager} when e device is add to the pool or removed.
 * 
 * @author yavor.stankov
 * 
 */
public class PoolManager extends UnicastRemoteObject implements IClientBuilder {
    private static final long serialVersionUID = -5077918124351182199L;

    private static final String DEVICE_RMI_ID_FORMAT = "%s_%s";

    private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

    private static PoolManager poolManagerInstance = null;

    private ServerEventService eventService = new ServerEventService();

    private IDevicePoolDao devicePoolDao = new DevicePoolDao();

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

        PasskeyAuthority passkeyAuthority = PasskeyAuthority.getInstance();
        passkeyAuthority.removeDevice(deviceProxy);

        DeviceInformation deviceInformation = (DeviceInformation) deviceProxy.route(RoutingAction.GET_DEVICE_INFORMATION);
        String deviceSerialNumber = deviceInformation.getSerialNumber();

        IDevice device = devicePoolDao.getDevice(deviceId);
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
    public String addDevice(String deviceRmiId, Registry agentRegistry, IAgentManager agentManager) {
        try {
            IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(deviceRmiId);

            DeviceInformation deviceInformation = (DeviceInformation) deviceWrapper.route(RoutingAction.GET_DEVICE_INFORMATION);

            String deviceSerialNumber = deviceInformation.getSerialNumber();
            String onAgentId = agentManager.getAgentId();

            String deviceId = buildDeviceIdentifier(onAgentId, deviceSerialNumber);

            DeviceProxy deviceProxy = new DeviceProxy(deviceWrapper, deviceId);

            DevicePublishEvent event = new DevicePublishedEvent(deviceProxy, deviceSerialNumber, onAgentId);
            eventService.publish(event);

            deviceIdToDeviceProxy.put(deviceId, deviceProxy);
            PasskeyAuthority passkeyAuthority = PasskeyAuthority.getInstance();
            long devicePasskey = passkeyAuthority.getPasskey(deviceProxy);

            try {
                devicePoolDao.addDevice(deviceInformation, deviceId, onAgentId, devicePasskey);
            } catch (DevicePoolDaoException e) {
                String errorMessage = String.format("Failed to add device with ID %s on agent %s.", deviceId, onAgentId);
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
        for (String deviceId : deviceIdToDeviceProxy.keySet()) {
            removeDevice(deviceId);
        }
    }

    @Override
    public synchronized DeviceAllocationInformation allocateDevice(DeviceParameters deviceParameters)
        throws RemoteException {
        List<IDevice> deviceList = new ArrayList<IDevice>();

        try {
            deviceList = devicePoolDao.getDevices(deviceParameters);
        } catch (DevicePoolDaoException e) {
            throw new NoAvailableDeviceFoundException();
        }

        if (deviceList.isEmpty()) {
            throw new NoAvailableDeviceFoundException();
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
        DeviceProxy deviceProxy = deviceIdToDeviceProxy.get(deviceId);

        // TODO: After the passkey will be stored for each device, think of a better solution for getting and validating
        // this key.
        PasskeyAuthority passkeyAuthority = PasskeyAuthority.getInstance();
        long devicePasskey = passkeyAuthority.getPasskey(deviceProxy);

        DeviceAllocationInformation allocatedDeviceDescriptor = new DeviceAllocationInformation(bestMatchDeviceRmiId,
                                                                                                devicePasskey,
                                                                                                deviceId);

        ClientRequestMonitor deviceMonitor = new ClientRequestMonitor();
        deviceMonitor.restartTimerForDevice(bestMatchDeviceRmiId);

        return allocatedDeviceDescriptor;
    }

    @Override
    public void releaseDevice(DeviceAllocationInformation allocatedDeviceDescriptor)
        throws RemoteException,
            InvalidPasskeyException {
        String deviceId = allocatedDeviceDescriptor.getDeviceId();
        DeviceProxy deviceProxy = deviceIdToDeviceProxy.get(deviceId);

        long passkey = allocatedDeviceDescriptor.getProxyPasskey();

        PasskeyAuthority passkeyAuthority = PasskeyAuthority.getInstance();
        passkeyAuthority.validatePasskey(deviceProxy, passkey);

        try {
            releaseDevice(deviceId);
        } catch (DevicePoolDaoException e) {
            String errorMessage = String.format("Failed to release device with ID %s.", deviceId);
            LOGGER.error(errorMessage);
        }
    }

    public void releaseDevice(String deviceId) throws RemoteException, DevicePoolDaoException {

        IDevice device = devicePoolDao.getDevice(deviceId);

        device.release();
        devicePoolDao.update(device);
    }

    public int getPoolSize() {
        return deviceIdToDeviceProxy.size();
    }

    private static String buildDeviceIdentifier(String onAgentId, String deviceSerialNumber) {
        String deviceIdentifier = String.format(DEVICE_RMI_ID_FORMAT, onAgentId, deviceSerialNumber);

        return deviceIdentifier;
    }
}
