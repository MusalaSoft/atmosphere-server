package com.musala.atmosphere.server.websocket;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.server.ServerManager;
import com.musala.atmosphere.server.pool.PoolManager;

public class ClientServerWebSocketCommunicator {
    private static final Logger LOGGER = Logger.getLogger(ClientServerWebSocketCommunicator.class.getCanonicalName());

    private static ClientServerWebSocketCommunicator instance = null;

    private PoolManager poolManager = PoolManager.getInstance();

    private ServerManager serverManager;

    private ClientServerWebSocketCommunicator() {
    }

    public static ClientServerWebSocketCommunicator getInstance() {
        if (instance == null) {
            synchronized (ClientServerWebSocketCommunicator.class) {
                if (instance == null) {
                    LOGGER.info("Creating new WebSocketCommunicator instance...");
                    instance = new ClientServerWebSocketCommunicator();
                }
            }
        }

        return instance;
    }

    /**
     * Returns {@link DeviceAllocationInformation} of a device for the provided selector.
     *
     * @param deviceSelector
     *        - the {@link DeviceSelector} with the parameters of the requested device
     * @return {@link DeviceAllocationInformation}
     * @throws NoDeviceMatchingTheGivenSelectorException
     *         - if a device corresponding to the provided selection properties is not present on the server
     * @throws NoAvailableDeviceFoundException
     *         - if a device was found but is currently allocated by another client
     */
    public DeviceAllocationInformation getDeviceAllocationInformation(DeviceSelector deviceSelector)
            throws NoDeviceMatchingTheGivenSelectorException, NoAvailableDeviceFoundException {
        return poolManager.allocateDevice(deviceSelector);
    }

    /**
     * Releases an allocated device.
     *
     * @param deviceDescriptor
     *        - the {@link DeviceAllocationInformation} corresponding to the device which should be released
     */
    public void releaseDevice(DeviceAllocationInformation deviceDescriptor) {
        poolManager.releaseDevice(deviceDescriptor);
    }

    /**
     * Sets the server manager for this communicator instance.
     *
     * @param serverManager
     *        - the {@link ServerManager} to be set
     */
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
