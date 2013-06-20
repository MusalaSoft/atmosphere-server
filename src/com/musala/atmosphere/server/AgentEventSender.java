package com.musala.atmosphere.server;

import java.rmi.RemoteException;

import com.musala.atmosphere.commons.sa.IAgentEventSender;

public class AgentEventSender implements IAgentEventSender
{
	private PoolManager poolManagerReference;

	public AgentEventSender(PoolManager poolManager)
	{
		poolManagerReference = poolManager;
	}

	@Override
	public void deviceListChanged(String agentId) throws RemoteException
	{
		poolManagerReference.onAgentDeviceListChanged(agentId);
	}

}
