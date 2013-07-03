package com.musala.atmosphere.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.musala.atmosphere.commons.sa.IAgentEventSender;

public class AgentEventSender extends UnicastRemoteObject implements IAgentEventSender
{
	/**
	 * auto-generated serialization version id.
	 */
	private static final long serialVersionUID = 3349681803074360222L;

	private PoolManager poolManagerReference;

	public AgentEventSender(PoolManager poolManager) throws RemoteException
	{
		poolManagerReference = poolManager;
	}

	@Override
	public void deviceListChanged(String agentId, String changedDeviceRmiId, boolean isNowAvailable)
		throws RemoteException
	{
		poolManagerReference.onAgentDeviceListChanged(agentId, changedDeviceRmiId, isNowAvailable);
	}

}
