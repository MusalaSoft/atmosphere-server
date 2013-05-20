package com.musala.atmosphere.server;

import java.rmi.RemoteException;

import com.musala.atmosphere.commons.sa.IAgentEventSender;

public class AgentEventSender implements IAgentEventSender
{
	public AgentEventSender()
	{

	}

	@Override
	public void deviceListChanged(String agentId) throws RemoteException
	{
		// TODO What should happen when deviceListChanged(agentID) was invoked by an agent?
	}

}
