package com.musala.atmosphere.server.pool;

import java.io.IOException;
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
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.cs.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.commons.cs.clientbuilder.IClientBuilder;
import com.musala.atmosphere.commons.cs.exception.EmulatorCreationFailedException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.sa.exceptions.TimeoutReachedException;
import com.musala.atmosphere.server.AgentAllocator;
import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.PasskeyAuthority;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.util.DeviceMatchingComparator;
import com.musala.atmosphere.server.util.EmulatorToDeviceParametersConverter;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

public class PoolManager extends UnicastRemoteObject implements IClientBuilder {
    private static final long serialVersionUID = -5077918124351182199L;

    private static final long EMULATOR_CREATION_TIMEOUT = ServerPropertiesLoader.getEmulatorCreationTimeout();

    private static final long POOL_ITEM_WAIT_INTERVAL = 1000;

    private static final String POOL_ITEM_ID_FORMAT = "%s_%s";

    private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

    private static PoolManager poolManagerInstance = null;

    private ServerEventService eventService = new ServerEventService();

    private HashMap<String, PoolItem> deviceIdToPoolItem = new HashMap<String, PoolItem>();

    private AgentAllocator agentAllocator = new AgentAllocator();

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
     * @param agent
     *        - the agent that the device is connected to.
     * @return True if the device is present, and false if it is not.
     */
    public boolean isDeviceConnectedToAgent(String agent) {
        for (PoolItem poolItem : deviceIdToPoolItem.values()) {
            if (poolItem.isCorrespondingTo(agent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Refreshes the current state of the device - removes the device from the pool if it is not present on an Agent
     * anymore and remove the device from the server's RMI registry.
     * 
     * @param changedDeviceId
     *        - string identifier on the Agent for the device wrapper stub.
     * @param agent
     *        - the {@link AgentManager AgentManager} that the device is connected to.
     */
    public void removeDevice(String changedDeviceId, String agent) {
        for (Entry<String, PoolItem> poolItems : deviceIdToPoolItem.entrySet()) {
            PoolItem poolItem = poolItems.getValue();
            String poolItemId = poolItems.getKey();

            if (!poolItem.isCorrespondingTo(agent)) {
                continue;
            }

            ClientRequestMonitor deviceMonitor = ClientRequestMonitor.getInstance();
            deviceMonitor.unregisterDevice(poolItemId);

            DeviceProxy deviceProxy = poolItem.getUnderlyingDeviceProxy();
            PasskeyAuthority passkeyAuthority = PasskeyAuthority.getInstance();
            passkeyAuthority.removeDevice(deviceProxy);

            DeviceInformation deviceInformation = poolItem.getUnderlyingDeviceInformation();
            String deviceSerialNumber = deviceInformation.getSerialNumber();

            Event event = new DeviceUnpublishedEvent(deviceProxy, deviceSerialNumber, agent);
            eventService.publish(event);

            deviceIdToPoolItem.remove(poolItemId);

            LOGGER.info("PoolItem with id " + changedDeviceId + " disconnected and removed.");
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
     */
    public void addDevice(String deviceRmiId, Registry agentRegistry, IAgentManager agentManager) {
        try {
            IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(deviceRmiId);
            DeviceProxy deviceProxy = new DeviceProxy(deviceWrapper);
            DeviceInformation deviceInformation = (DeviceInformation) deviceWrapper.route(RoutingAction.GET_DEVICE_INFORMATION);

            String deviceSerialNumber = deviceInformation.getSerialNumber();
            String onAgentId = agentManager.getAgentId();

            Event devicePublishedEvent = new DevicePublishedEvent(deviceProxy, deviceSerialNumber, onAgentId);
            eventService.publish(devicePublishedEvent);

            PoolItem poolItem = new PoolItem(deviceProxy, deviceInformation, onAgentId);
            String poolItemId = buildPoolItemIdentifier(onAgentId, deviceSerialNumber);
            deviceIdToPoolItem.put(poolItemId, poolItem);

            ClientRequestMonitor deviceMonitor = ClientRequestMonitor.getInstance();
            deviceMonitor.registerDevice(poolItemId);

            LOGGER.info("PoolItem created with rmi id [" + poolItemId + "].");
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
        for (PoolItem poolItem : deviceIdToPoolItem.values()) {
            DeviceProxy deviceProxy = poolItem.getUnderlyingDeviceProxy();

            DeviceInformation deviceInformation = poolItem.getUnderlyingDeviceInformation();
            String deviceSerialNumber = deviceInformation.getSerialNumber();

            String onAgentId = poolItem.getUnderlyingAgentId();

            Event deviceUnpublishedEvent = new DeviceUnpublishedEvent(deviceProxy, deviceSerialNumber, onAgentId);
            eventService.publish(deviceUnpublishedEvent);
        }
    }

    /**
     * Gets a {@link DeviceInformation}, {@link PoolItem} mapping of all {@link PoolItem}s matching given
     * {@link DeviceParameters}.
     * 
     * @param deviceParameters
     *        - the {@link DeviceParameters} that will be used for comparison.
     * @return a {@link DeviceInformation}, {@link PoolItem} mapping of all {@link PoolItem}s matching given
     *         {@link DeviceParameters}.
     */
    private Map<DeviceInformation, PoolItem> getFreePoolItemsMatchingParameters(DeviceParameters deviceParameters) {
        Map<DeviceInformation, PoolItem> freePoolItemsDeviceInfoMap = new HashMap<DeviceInformation, PoolItem>();
        for (PoolItem poolItem : deviceIdToPoolItem.values()) {
            if (poolItem.isAvailable()) {
                DeviceInformation poolItemDeviceInformation = poolItem.getUnderlyingDeviceInformation();

                if (!DeviceMatchingComparator.isValidMatch(deviceParameters, poolItemDeviceInformation)) {
                    continue;
                }

                freePoolItemsDeviceInfoMap.put(poolItemDeviceInformation, poolItem);
            }
        }

        return freePoolItemsDeviceInfoMap;
    }

    /**
     * Gets the {@link PoolItem} with highest match score for given {@link DeviceParameters}.
     * 
     * @param deviceParameters
     *        - the {@link DeviceParameters} that will be used for comparison.
     * @return the {@link PoolItem} with highest match score for given {@link DeviceParameters}.
     */
    private PoolItem getBestMatchingFreePoolItem(DeviceParameters deviceParameters) {
        Map<DeviceInformation, PoolItem> freePoolItemsDeviceInfoMap = getFreePoolItemsMatchingParameters(deviceParameters);
        PoolItem bestMatchingPoolItem = null;

        if (!freePoolItemsDeviceInfoMap.isEmpty()) {
            DeviceMatchingComparator matchComparator = new DeviceMatchingComparator(deviceParameters);

            DeviceInformation bestMatchDeviceInformation = null;
            try {
                bestMatchDeviceInformation = Collections.max(freePoolItemsDeviceInfoMap.keySet(), matchComparator);
                bestMatchingPoolItem = freePoolItemsDeviceInfoMap.get(bestMatchDeviceInformation);
            } catch (NoSuchElementException e) {
                // Nothing to do here
            }
        }

        return bestMatchingPoolItem;
    }

    @Override
    public synchronized DeviceAllocationInformation allocateDevice(DeviceParameters deviceParameters)
        throws RemoteException {
        // TODO: refactor synchronization.
        DeviceType deviceType = deviceParameters.getDeviceType();
        boolean isDeviceOnly = deviceType == DeviceType.DEVICE_ONLY;
        PoolItem bestMatchingPoolItem = getBestMatchingFreePoolItem(deviceParameters);
        boolean isBestMatchingPoolItemNull = bestMatchingPoolItem == null;

        if (isBestMatchingPoolItemNull && isDeviceOnly) {
            String message = "No available device found.";
            LOGGER.info(message);
            throw new NoAvailableDeviceFoundException(message);
        }

        if (isBestMatchingPoolItemNull) {
            LOGGER.info("No available device found. Starting an emulator device...");
            bestMatchingPoolItem = createEmulator(deviceParameters);
        }
        String onAgentId = bestMatchingPoolItem.getUnderlyingAgentId();

        DeviceInformation deviceInformation = bestMatchingPoolItem.getUnderlyingDeviceInformation();
        String deviceSerialNumber = deviceInformation.getSerialNumber();

        String bestMatchDeviceProxyRmiId = buildPoolItemIdentifier(onAgentId, deviceSerialNumber);
        bestMatchingPoolItem.setAvailability(false);
        DeviceProxy selectedPoolItemDeviceProxy = bestMatchingPoolItem.getUnderlyingDeviceProxy();
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

        PoolItem poolItem = deviceIdToPoolItem.get(rmiId);
        if (poolItem != null) {
            DeviceProxy releasedDeviceProxy = poolItem.getUnderlyingDeviceProxy();
            PasskeyAuthority.getInstance().validatePasskey(releasedDeviceProxy, passkey);

            releasePoolItem(rmiId);
        }
    }

    /**
     * Frees given PoolItem by its RMI identifier so it can be given to the next Client.
     * 
     * @param poolItemIdentifier
     */
    void releasePoolItem(String poolItemIdentifier) {
        PoolItem poolItemForRelease = deviceIdToPoolItem.get(poolItemIdentifier);

        if (poolItemForRelease != null) {
            poolItemForRelease.setAvailability(true);
            DeviceProxy deviceProxyToRelease = poolItemForRelease.getUnderlyingDeviceProxy();
            PasskeyAuthority.getInstance().renewPasskey(deviceProxyToRelease);

            LOGGER.info("Released device with rmi id " + poolItemIdentifier);
        } else {
            LOGGER.fatal("Error trying to unexport PoolItem: PoolItem with ID: " + poolItemIdentifier + " not found.");
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
        if (!deviceIdToPoolItem.containsKey(deviceProxyRmiId)) {
            LOGGER.error("Checking if device is in use for unregistered proxy ID [" + deviceProxyRmiId + "].");
            return true;
        }
        PoolItem correspondingPoolItem = deviceIdToPoolItem.get(deviceProxyRmiId);
        boolean availability = correspondingPoolItem.isAvailable();
        return (!availability);
    }

    /**
     * Returns the {@link DeviceProxy} object with given RMI identifier.
     * 
     * @param poolItemIdentifier
     * @return - {@link DeviceProxy} that lies under the {@link PoolItem} with the passed RMI id.
     */
    DeviceProxy getUnderlyingDeviceProxy(String poolItemIdentifier) {
        PoolItem poolItem = deviceIdToPoolItem.get(poolItemIdentifier);
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
        deviceProxyIds.addAll(deviceIdToPoolItem.keySet());
        return deviceProxyIds;
    }

    private String waitForPoolItemExists(String serialNumber, long timeout) throws TimeoutReachedException {
        while (timeout > 0) {
            for (Entry<String, PoolItem> currentRmiIdPoolItemEntry : deviceIdToPoolItem.entrySet()) {
                String currentRmiId = currentRmiIdPoolItemEntry.getKey();
                PoolItem currentPoolItem = currentRmiIdPoolItemEntry.getValue();
                DeviceInformation currentPoolItemInformation = currentPoolItem.getUnderlyingDeviceInformation();
                String currentPoolItemSerial = currentPoolItemInformation.getSerialNumber();
                if (serialNumber.equals(currentPoolItemSerial)) {
                    return currentRmiId;
                }
            }
            try {
                Thread.sleep(POOL_ITEM_WAIT_INTERVAL);
                timeout -= POOL_ITEM_WAIT_INTERVAL;
            } catch (InterruptedException e) {
                // Nothing to do here
            }
        }

        throw new TimeoutReachedException();
    }

    /**
     * Creates an emulator device matching the given {@link DeviceParameters} to be published to the server.
     * 
     * @param deviceParameters
     *        - {@link DeviceParameters} that will be used for emulator creation.
     * @return a {@link PoolItem} representing the created emulator device.
     * @throws EmulatorCreationFailedException
     */
    private PoolItem createEmulator(DeviceParameters deviceParameters) throws EmulatorCreationFailedException {
        IAgentManager agentManager = agentAllocator.getAgent();
        EmulatorParameters emulatorParameters = EmulatorToDeviceParametersConverter.convert(deviceParameters);

        // TODO: think of a better logic here.
        // TODO: extract this logic.
        long waitForEmulatorExistsTimeout = EMULATOR_CREATION_TIMEOUT * 5 / 30;
        long waitForDeviceExistsTimeout = EMULATOR_CREATION_TIMEOUT * 4 / 5;
        long waitForPoolItemExistsTimeout = EMULATOR_CREATION_TIMEOUT * 1 / 30;

        String errorMessage = null;
        try {
            errorMessage = "Emulator creation failed.";
            String emulatorName = agentManager.createAndStartEmulator(emulatorParameters);
            LOGGER.info("Created emulator device with name " + emulatorName);

            LOGGER.info("Waiting for emulator process to start...");
            errorMessage = "Emulator wait timeout reached.";
            agentManager.waitForEmulatorExists(emulatorName, waitForEmulatorExistsTimeout);
            LOGGER.info("Emulator process started.");

            errorMessage = "Getting serial number of created emulator failed.";
            String serialNumber = agentManager.getSerialNumberOfEmulator(emulatorName);

            LOGGER.info("Waiting for emulator to be wrapped...");
            errorMessage = "Emulator wrapping wait timeout reached.";
            agentManager.waitForDeviceExists(serialNumber, waitForDeviceExistsTimeout);
            LOGGER.info("Emulator device wrapped.");

            LOGGER.info("Waiting for emulator wrapper to be published to server...");
            errorMessage = "Emulator server publishing wait timeout reached.";
            String poolItemId = waitForPoolItemExists(serialNumber, waitForPoolItemExistsTimeout);
            LOGGER.info("Emulator wrapped published to server.");

            return deviceIdToPoolItem.get(poolItemId);
        } catch (IOException | TimeoutReachedException | DeviceNotFoundException e) {
            LOGGER.fatal(errorMessage, e);
            throw new EmulatorCreationFailedException(errorMessage, e);
        }
    }

    private String buildPoolItemIdentifier(String onAgentId, String deviceSerialNumber) {
        String poolItemIdentifier = String.format(POOL_ITEM_ID_FORMAT, onAgentId, deviceSerialNumber);
        return poolItemIdentifier;
    }
}
