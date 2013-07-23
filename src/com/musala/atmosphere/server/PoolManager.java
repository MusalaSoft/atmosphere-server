package com.musala.atmosphere.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.IClientBuilder;
import com.musala.atmosphere.commons.sa.DeviceInformation;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.util.DeviceMatchingComparator;

/**
 * Class that is responsible for managing device selection/distribution.
 * 
 * @author georgi.gaydarov
 * 
 */
public class PoolManager extends UnicastRemoteObject implements IClientBuilder
{
	/**
	 * auto-generated serialization version id.
	 */
	private static final long serialVersionUID = -3509684187196223780L;

	private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

	private List<PoolItem> poolItems = new LinkedList<PoolItem>();

	private Map<String, IAgentManager> agentManagersId = new HashMap<String, IAgentManager>();

	private Map<IAgentManager, Registry> agentManagerRegistry = new HashMap<IAgentManager, Registry>();

	private int rmiRegistryPort;

	private Registry rmiRegistry;

	private AgentEventSender agentChangeNotifier;

	private ConnectionRequestReceiver connectionRequestReceiver;

	void onAgentDeviceListChanged(String onAgent, String changedDeviceRmiId, boolean isNowAvailable)
	{
		if (agentManagersId.containsKey(onAgent) == false)
		{
			LOGGER.warn("Received device state change event from an Agent that is not registered on the PoolManager ("
					+ onAgent + ").");
			return;
		}

		// TODO make this more complex - what happens if a device that is allocated to a client is disconnected?
		for (PoolItem poolItem : poolItems)
		{
			if (poolItem.isUnderlyingDeviceWrapperAsArguments(onAgent, changedDeviceRmiId))
			{
				if (isNowAvailable)
				{
					LOGGER.warn("Received device connected event for a device that is already registered.");
				}
				else
				{
					poolItem.unbindDeviceProxyFromRmi();
					poolItems.remove(poolItem);
				}
				return;
			}
		}

		if (isNowAvailable == false)
		{
			LOGGER.warn("Received device disconnected event for a device that was not registered at all.");
		}
		else
		{
			publishDeviceProxy(changedDeviceRmiId, onAgent);
		}
	}

	/**
	 * Creates a new {@link PoolManager PoolManager} instance that opens an RMI registry on a specific port and waits
	 * for a client connection.
	 * 
	 * @param rmiPort
	 *        port, on which the RMI registry for the new {@link PoolManager PoolManager} will be opened.
	 * @throws RemoteException
	 */
	public PoolManager(int rmiPort) throws RemoteException
	{
		// Publish this PoolManager in the RMI registry
		try
		{
			rmiRegistryPort = rmiPort;
			rmiRegistry = LocateRegistry.createRegistry(rmiPort);
			String poolManagerRmiPublishString = com.musala.atmosphere.commons.cs.RmiStringConstants.POOL_MANAGER.toString();
			rmiRegistry.rebind(poolManagerRmiPublishString, this);
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
		connectionRequestReceiver.close();
		try
		{
			// Close the registry
			if (rmiRegistry != null)
			{
				UnicastRemoteObject.unexportObject(rmiRegistry, true);
			}
		}
		catch (Exception e)
		{
			// If something cannot be closed it was never opened, so it's okay.
			// Nothing to do here.
			e.printStackTrace();
		}
		LOGGER.info("PoolManager instance closed.");
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
		for (String wrapperRmiId : deviceWrappers)
		{
			publishDeviceProxy(wrapperRmiId, agentId);
		}
	}

	private void publishDeviceProxy(String deviceWrapperId, String onAgentId)
	{
		IAgentManager onAgent = agentManagersId.get(onAgentId);
		Registry agentRegistry = agentManagerRegistry.get(onAgent);
		IWrapDevice deviceWrapper = null;
		try
		{
			deviceWrapper = (IWrapDevice) agentRegistry.lookup(deviceWrapperId);
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

		try
		{
			PoolItem poolItem = new PoolItem(deviceWrapperId, deviceWrapper, onAgent, rmiRegistryPort);
			poolItems.add(poolItem);
		}
		catch (RemoteException e)
		{
			LOGGER.warn("PoolItem instance creation resulted in a RemoteException.", e);
		}
	}

	@Override
	public String getDeviceProxyRmiId(DeviceParameters deviceParameters) throws RemoteException
	{
		Map<DeviceInformation, PoolItem> freePoolItemsDeviceInfoMap = new HashMap<DeviceInformation, PoolItem>();
		List<DeviceInformation> freePoolItemsDeviceInfoList = new ArrayList<DeviceInformation>();
		for (PoolItem poolItem : poolItems)
		{
			// TODO Implement pooling mechanism
			// if(pool.isavail(poolItem) == false) continue;
			DeviceInformation poolItemDeviceInformation = poolItem.getUnderlyingDeviceInformation();
			if (DeviceMatchingComparator.isValidMatch(deviceParameters, poolItemDeviceInformation) == false)
			{
				continue;
			}

			freePoolItemsDeviceInfoMap.put(poolItemDeviceInformation, poolItem);
			freePoolItemsDeviceInfoList.add(poolItemDeviceInformation);
		}

		if (freePoolItemsDeviceInfoList.size() == 0)
		{
			// TODO add logic behind device creation
		}

		DeviceMatchingComparator matchComparator = new DeviceMatchingComparator(deviceParameters);

		// TODO implement pooling .get
		DeviceInformation bestMatchDeviceInformation = Collections.max(freePoolItemsDeviceInfoList, matchComparator);
		PoolItem bestMatchPoolItem = freePoolItemsDeviceInfoMap.get(bestMatchDeviceInformation);
		String bestMatchDeviceProxyRmiId = bestMatchPoolItem.getDeviceProxyRmiBindingIdentifier();

		return bestMatchDeviceProxyRmiId;
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

	/**
	 * Gets the list of all published {@link DeviceProxy DeviceProxy} instance IDs.
	 * 
	 * @return List<String> of device proxy IDs.
	 */
	public List<String> getAllDeviceProxyIds()
	{
		List<String> deviceProxyIds = new LinkedList<String>();

		for (PoolItem poolItem : poolItems)
		{
			String deviceProxyRmiId = poolItem.getDeviceProxyRmiBindingIdentifier();
			deviceProxyIds.add(deviceProxyRmiId);
		}
		return deviceProxyIds;
	}

	/**
	 * Checks if a {@link DeviceProxy DeviceProxy} for a specified device ID on a specified Agent is published.
	 * 
	 * @param onAgentId
	 *        device descriptor - the Agent ID of the Agent it is connected to.
	 * @param onAgentDeviceId
	 *        device descriptor - the device's ID as is on the Agent.
	 * @return
	 */
	public boolean isSuchDeviceProxyPresent(String onAgentId, String onAgentDeviceId)
	{
		for (PoolItem poolItem : poolItems)
		{
			if (poolItem.isUnderlyingDeviceWrapperAsArguments(onAgentId, onAgentDeviceId))
			{
				return true;
			}
		}
		return false;
	}
}
