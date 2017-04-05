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

    public DeviceAllocationInformation getDeviceAllocationInformation(DeviceSelector deviceSelector)
            throws NoDeviceMatchingTheGivenSelectorException, NoAvailableDeviceFoundException {
        PoolManager poolManager = PoolManager.getInstance();

        return poolManager.allocateDevice(deviceSelector);
    }

    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
