package com.musala.atmosphere.server;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Class that is responsible for managing device selection/distribution.
 * 
 * @author georgi.gaydarov
 * 
 */
public class ServerManager
{

	private static Logger LOGGER = Logger.getLogger(ServerManager.class.getCanonicalName());

	private Map<String, IAgentManager> agentManagersId = new HashMap<String, IAgentManager>();

	private Map<IAgentManager, Registry> agentManagerRegistry = new HashMap<IAgentManager, Registry>();

	private int rmiRegistryPort;

	private Registry rmiRegistry;

	private AgentEventSender agentChangeNotifier;

	private ConnectionRequestReceiver connectionRequestReceiver;

	private PoolManager poolManager = PoolManager.getInstance();

	void onAgentDeviceListChanged(String onAgent, String changedDeviceRmiId, boolean isConnected)
	{
		if (!agentManagersId.containsKey(onAgent))
		{
			// The agent which sends the event is not registered on server
			LOGGER.warn("Received device state change event from an Agent that is not registered on the server ("
					+ onAgent + ").");
			return;
		}
		else
		{
			// The agent which sends the event is registered to the server
			if (poolManager.isDevicePresent(changedDeviceRmiId, onAgent))
			{
				poolManager.refreshDevice(changedDeviceRmiId, onAgent, isConnected);
			}
			else
			{
				// TODO make this more complex - what happens if a device that is allocated to a client is disconnected?
				if (isConnected)
				{
					IAgentManager agent = agentManagersId.get(onAgent);
					Registry agentRegistry = agentManagerRegistry.get(agent);
					try
					{
						IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(changedDeviceRmiId);
						poolManager.addDevice(changedDeviceRmiId, deviceWrapper, agent, rmiRegistryPort);
					}
					catch (NotBoundException e)
					{
						LOGGER.warn("Attempted to get a non-bound device wrapper from an Agent.", e);
						return;
					}
					catch (RemoteException e)
					{
						LOGGER.warn("Attempted to get a device wrapper from an Agent that we can not connect to.", e);
						return;
					}
				}
				else
				{
					LOGGER.warn("Received device disconnected event for a device that was not registered at all.");
				}
			}

		}
	}

	/**
	 * Creates a new {@link ServerManager ServerManager} instance that opens an RMI registry on a specific port and
	 * waits for a client connection.
	 * 
	 * @param rmiPort
	 *        port, on which the RMI registry for the new {@link ServerManager SercerManager} will be opened.
	 * @throws RemoteException
	 */
	public ServerManager(int rmiPort) throws RemoteException
	{
		// Publish this ServerManager in the RMI registry
		try
		{
			rmiRegistryPort = rmiPort;
			rmiRegistry = LocateRegistry.createRegistry(rmiPort);

			String poolManagerRmiPublishString = com.musala.atmosphere.commons.cs.RmiStringConstants.POOL_MANAGER.toString();
			rmiRegistry.rebind(poolManagerRmiPublishString, poolManager);

			LOGGER.info("PoolManager instance published in RMI (port " + rmiPort + ") under the identifier '"
					+ poolManagerRmiPublishString + "'.");

			String connectionRequestReceiverRmiString = RmiStringConstants.CONNECTION_REQUEST_RECEIVER.toString();
			connectionRequestReceiver = new ConnectionRequestReceiver(this);
			rmiRegistry.rebind(connectionRequestReceiverRmiString, connectionRequestReceiver);

			LOGGER.info("Connection request receiver instance published in RMI under the identifier '"
					+ connectionRequestReceiverRmiString + "'.");
		}
		catch (RemoteException e)
		{
			close();
			throw e;
		}

		// Publish an AgentEventSender in the RMI registry
		try
		{
			agentChangeNotifier = new AgentEventSender(this);
			String agentChangeNotifierRmiPublishString = RmiStringConstants.AGENT_EVENT_SENDER.toString();
			rmiRegistry.rebind(agentChangeNotifierRmiPublishString, agentChangeNotifier);
			LOGGER.info("AgentEventSender instance published in RMI (port " + rmiPort + ") under the identifier '"
					+ agentChangeNotifierRmiPublishString + "'.");
		}
		catch (RemoteException e)
		{
			close();
			throw e;
		}
	}

	/**
	 * Calls the {@link #close() close()} method just to be sure everything is closed.
	 */
	@Override
	public void finalize()
	{
		close();
	}

	/**
	 * Closes all open resources. <b>MUST BE CALLED WHEN THIS CLASS IS NO LONGER NEEDED.</b>
	 */
	public void close()
	{
		if (connectionRequestReceiver != null)
		{
			connectionRequestReceiver.close();
			connectionRequestReceiver = null;
		}
		try
		{
			// Close the registry
			if (rmiRegistry != null)
			{
				// unexport the poolItems one by one
				poolManager.unexportAllPoolItems();

				// unexport everything else
				String[] rmiObjectIds = rmiRegistry.list();
				for (String rmiObjectId : rmiObjectIds)
				{
					Object obj = rmiRegistry.lookup(rmiObjectId);
					try
					{
						rmiRegistry.unbind(rmiObjectId);
						UnicastRemoteObject.unexportObject((Remote) obj, true);
					}
					catch (NoSuchObjectException e)
					{
						LOGGER.warn("No such object.", e);
					}
				}
			}
			UnicastRemoteObject.unexportObject(rmiRegistry, true);
		}
		catch (Exception e)
		{
			// If something cannot be closed it was never opened, so it's okay.
			// Nothing to do here.
			e.printStackTrace();
		}
		LOGGER.info("ServerManager instance closed.");
	}

	/**
	 * Connects to an Agent and adds it to the internal list of available agents to work with.
	 * 
	 * @param ip
	 *        address of the agent we want to connect to.
	 * @param port
	 *        port that the agent has created it's registry on.
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void connectToAgent(String ip, int port) throws RemoteException, NotBoundException
	{
		String agentId = connectToAndRegisterAgent(ip, port);
		LOGGER.info("Connection to Agent with address [" + ip + ":" + port + "] established.");
		publishAllDeviceProxiesForAgent(agentId);
	}

	private String connectToAndRegisterAgent(String ip, int port) throws RemoteException, NotBoundException
	{
		// Get the agent rmi stub
		Registry agentRegistry = LocateRegistry.getRegistry(ip, port);
		IAgentManager agent = (IAgentManager) agentRegistry.lookup(RmiStringConstants.AGENT_MANAGER.toString());

		// Add the agent stub to the agent lists
		String agentId = agent.getAgentId();
		agentManagersId.put(agentId, agent);
		agentManagerRegistry.put(agent, agentRegistry);

		// Register the server for event notifications
		String serverIpForAgent = agent.getInvokerIpAddress();
		agent.registerServer(serverIpForAgent, rmiRegistryPort);
		return agentId;
	}

	private void publishAllDeviceProxiesForAgent(String agentId) throws RemoteException
	{
		IAgentManager agent = agentManagersId.get(agentId);
		List<String> deviceWrappers = agent.getAllDeviceWrappers();
		Registry agentRegistry = agentManagerRegistry.get(agent);
		for (String wrapperRmiId : deviceWrappers)
		{
			try
			{
				IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(wrapperRmiId);
				poolManager.addDevice(wrapperRmiId, deviceWrapper, agent, rmiRegistryPort);
			}
			catch (NotBoundException e)
			{
				LOGGER.error("Could not find device to wrap on the Agent.", e);
			}
		}
	}

	/**
	 * Gets the list of all connected Agent IDs.
	 * 
	 * @return List<String> of Agent IDs.
	 */
	public List<String> getAllConnectedAgentIds()
	{
		List<String> agentIds = new LinkedList<String>();

		for (Entry<String, IAgentManager> idAgentPair : agentManagersId.entrySet())
		{
			String agentId = idAgentPair.getKey();
			agentIds.add(agentId);
		}
		return agentIds;
	}
}
