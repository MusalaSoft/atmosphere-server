package com.musala.atmosphere.server.pool;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
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
import com.musala.atmosphere.server.util.DeviceMatchingComparator;

public class PoolManager extends UnicastRemoteObject implements IClientBuilder {
    /**
     * auto-generated serialization id
     */
    private static final long serialVersionUID = -5077918124351182199L;

    private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

    private static PoolManager poolManagerInstance = null;

    private HashMap<String, PoolItem> rmiIdToPoolItem = new HashMap<String, PoolItem>();

    private PoolManager() throws RemoteException {
    }

    /**
     * Creates an instance (or gets the current if such instance already exists) of the {@link PoolManager PoolManager}.
     * 
     * @param serverManager
     *        - the ServerManager for which we want to create/get an instance of PoolManager.
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
     * Checks whether a device is present in the pool.
     * 
     * @param changedDeviceRmiId
     *        - RMI string identifier for the device.
     * @param agent
     *        - the agent that the device is connected to.
     * @return True if the device is present, and false if it is not.
     */
    public boolean isDevicePresent(String changedDeviceRmiId, String agent) {
        for (PoolItem poolItem : rmiIdToPoolItem.values()) {
            if (poolItem.isCorrespondingTo(agent, changedDeviceRmiId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Refreshes the current state of the device - removes the device from the pool if it is not present on an Agent
     * anymore.
     * 
     * @param changedDeviceRmiId
     *        - RMI string identifier on the Agent for the device wrapper stub.
     * @param agent
     *        - the {@link AgentManager AgentManager} that the device is connected to.
     */
    public void removeDevice(String changedDeviceRmiId, String agent) {
        for (Entry<String, PoolItem> poolItems : rmiIdToPoolItem.entrySet()) {
            PoolItem poolItem = poolItems.getValue();
            String poolItemProxyRmiId = poolItems.getKey();

            if (!poolItem.isCorrespondingTo(agent, changedDeviceRmiId)) {
                continue;
            }

            ClientRequestMonitor deviceMonitor = ClientRequestMonitor.getInstance();
            deviceMonitor.unregisterDevice(poolItemProxyRmiId);

            DeviceProxy removedProxy = poolItem.getUnderlyingDeviceProxy();
            PasskeyAuthority.getInstance().removeDevice(removedProxy);
            poolItem.unbindDeviceProxyFromRmi();
            rmiIdToPoolItem.remove(poolItemProxyRmiId);

            LOGGER.info("PoolItem with id " + changedDeviceRmiId + " disconnected and removed.");
            return;
        }
        LOGGER.warn("Received remove request for a device that is not present in the pool.");
    }

    /**
     * Adds a device to the pool.
     * 
     * @param deviceRmiId
     *        - RMI string identifier for the device wrapper stub on the Agent registry.
     * @param agentRegistry
     *        - the Agent {@link Registry} object that contains the device that will be added.
     * @param agentManager
     *        - the {@link AgentManager AgentManager} that published the {@link IWrapDevice IWrapDevice} we are
     *        wrapping.
     * @param rmiRegistryPort
     *        - RMI registry port in which we will publish the newly created {@link DeviceProxy DeviceProxy} wrapper.
     */
    public void addDevice(String deviceRmiId, Registry agentRegistry, IAgentManager agentManager, int rmiRegistryPort) {
        try {
            IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(deviceRmiId);

            PoolItem poolItem = new PoolItem(deviceRmiId, deviceWrapper, agentManager, rmiRegistryPort);
            String poolItemRmiId = poolItem.getDeviceProxyRmiBindingIdentifier();
            rmiIdToPoolItem.put(poolItemRmiId, poolItem);

            ClientRequestMonitor deviceMonitor = ClientRequestMonitor.getInstance();
            deviceMonitor.registerDevice(poolItemRmiId);

            LOGGER.info("PoolItem created with rmi id [" + poolItemRmiId + "].");
        } catch (NotBoundException e) {
            LOGGER.warn("Attempted to get a non-bound device wrapper [" + deviceRmiId + "] from an Agent.", e);
        } catch (RemoteException e) {
            LOGGER.warn("Attempted to get a device wrapper from an Agent that we can not connect to.", e);
        } catch (CommandFailedException e) {
            LOGGER.error("Attemted to create a pool item for a device that fails to supply describing information.", e);
        }
    }

    /**
     * Unexports all devices from the pool.
     */
    public void unexportAllPoolItems() {
        for (PoolItem poolItem : rmiIdToPoolItem.values()) {
            poolItem.unbindDeviceProxyFromRmi();
        }
    }

    @Override
    public synchronized DeviceAllocationInformation allocateDevice(DeviceParameters deviceParameters)
        throws RemoteException {
        Map<DeviceInformation, PoolItem> freePoolItemsDeviceInfoMap = new HashMap<DeviceInformation, PoolItem>();
        List<DeviceInformation> freePoolItemsDeviceInfoList = new ArrayList<DeviceInformation>();
        for (PoolItem poolItem : rmiIdToPoolItem.values()) {
            if (poolItem.isAvailable()) {
                DeviceInformation poolItemDeviceInformation = poolItem.getUnderlyingDeviceInformation();

                if (!DeviceMatchingComparator.isValidMatch(deviceParameters, poolItemDeviceInformation)) {
                    continue;
                }

                freePoolItemsDeviceInfoMap.put(poolItemDeviceInformation, poolItem);
                freePoolItemsDeviceInfoList.add(poolItemDeviceInformation);
            }
        }

        if (freePoolItemsDeviceInfoList.size() == 0) {
            // TODO add logic behind device creation
            LOGGER.warn("No available devices found.");
        }

        DeviceMatchingComparator matchComparator = new DeviceMatchingComparator(deviceParameters);

        DeviceInformation bestMatchDeviceInformation = null;
        try {
            bestMatchDeviceInformation = Collections.max(freePoolItemsDeviceInfoList, matchComparator);
        } catch (NoSuchElementException e) {
            LOGGER.info("No available device found.");
            throw new NoAvailableDeviceFoundException("No available device found.");

        }

        PoolItem bestMatchPoolItem = freePoolItemsDeviceInfoMap.get(bestMatchDeviceInformation);
        String bestMatchDeviceProxyRmiId = bestMatchPoolItem.getDeviceProxyRmiBindingIdentifier();

        bestMatchPoolItem.setAvailability(false);
        DeviceProxy selectedPoolItemDeviceProxy = bestMatchPoolItem.getUnderlyingDeviceProxy();
        long devicePasskey = PasskeyAuthority.getInstance().getPasskey(selectedPoolItemDeviceProxy);

        DeviceAllocationInformation allocatedDeviceDescriptor = new DeviceAllocationInformation(bestMatchDeviceProxyRmiId,
                                                                                                devicePasskey);

        ClientRequestMonitor deviceMonitor = ClientRequestMonitor.getInstance();
        deviceMonitor.restartTimerForDevice(selectedPoolItemDeviceProxy);

        return allocatedDeviceDescriptor;
    }

    @Override
    public void releaseDevice(DeviceAllocationInformation allocatedDeviceDescriptor)
        throws RemoteException,
            InvalidPasskeyException {
        String rmiId = allocatedDeviceDescriptor.getProxyRmiId();
        long passkey = allocatedDeviceDescriptor.getProxyPasskey();

        PoolItem poolItem = rmiIdToPoolItem.get(rmiId);
        if (poolItem != null) {
            DeviceProxy releasedDeviceProxy = poolItem.getUnderlyingDeviceProxy();
            PasskeyAuthority.getInstance().validatePasskey(releasedDeviceProxy, passkey);

            releasePoolItem(rmiId);
        }
    }

    /**
     * Frees given PoolItem by its RMI identifier so it can be given to the next Client.
     * 
     * @param poolItemRmiId
     */
    void releasePoolItem(String poolItemRmiId) {
        PoolItem poolItemForRelease = rmiIdToPoolItem.get(poolItemRmiId);

        if (poolItemForRelease != null) {
            poolItemForRelease.setAvailability(true);
            DeviceProxy deviceProxyToRelease = poolItemForRelease.getUnderlyingDeviceProxy();
            PasskeyAuthority.getInstance().renewPasskey(deviceProxyToRelease);

            LOGGER.info("Released device with rmi id " + poolItemRmiId);
        } else {
            LOGGER.fatal("Error trying to unexport PoolItem: PoolItem with ID: " + poolItemRmiId + " not found.");
        }
    }

    /**
     * Checks a device by its RMI identifier if it is currently being used.
     * 
     * @param deviceProxyRmiId
     *        - RMI id of the device we seek
     * @return - true, if the device is currently used by a Client, and false otherwise.
     */
    boolean isInUse(String deviceProxyRmiId) {
        if (!rmiIdToPoolItem.containsKey(deviceProxyRmiId)) {
            LOGGER.error("Checking if device is in use for unregistered proxy ID [" + deviceProxyRmiId + "].");
            return true;
        }
        PoolItem correspondingPoolItem = rmiIdToPoolItem.get(deviceProxyRmiId);
        boolean availability = correspondingPoolItem.isAvailable();
        return (!availability);
    }

    /**
     * Returns the {@link DeviceProxy} object with given RMI identifier.
     * 
     * @param deviceProxyRmiBindingIdentifier
     * @return - {@link DeviceProxy} that lies under the {@link PoolItem} with the passed RMI id.
     */
    DeviceProxy getUnderlyingDeviceProxy(String deviceProxyRmiBindingIdentifier) {
        PoolItem poolItem = rmiIdToPoolItem.get(deviceProxyRmiBindingIdentifier);
        DeviceProxy underlyingDeviceProxy = poolItem.getUnderlyingDeviceProxy();
        return underlyingDeviceProxy;
    }

    /**
     * Gets a list of all published {@link DeviceProxy DeviceProxy} instance IDs.
     * 
     * @return List<String> of all device proxy IDs present in the device pool.
     */
    public List<String> getAllUnderlyingDeviceProxyIds() {
        List<String> deviceProxyIds = new ArrayList<String>();
        deviceProxyIds.addAll(rmiIdToPoolItem.keySet());
        return deviceProxyIds;
    }
}
