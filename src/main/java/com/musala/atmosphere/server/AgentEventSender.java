package com.musala.atmosphere.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IAgentEventSender;

/**
 * 
 * @author georgi.gaydarov
 * 
 */
public class AgentEventSender extends UnicastRemoteObject implements IAgentEventSender {
    /**
     * auto-generated serialization version id.
     */
    private static final long serialVersionUID = 3349681803074360222L;

    private ServerManager serverManager;

    public AgentEventSender(ServerManager serverManager) throws RemoteException {
        this.serverManager = serverManager;
    }

    @Override
    public void deviceListChanged(String agentId, String changedDeviceRmiId, boolean isNowAvailable)
        throws RemoteException,
            CommandFailedException,
            NotBoundException {
        serverManager.onAgentDeviceListChanged(agentId, changedDeviceRmiId, isNowAvailable);
    }

    @Override
    public void updateDevice(String agentId, String changedDeviceRmiId) throws RemoteException {
        serverManager.onDeviceInformationChanged(agentId, changedDeviceRmiId);
    }

}
