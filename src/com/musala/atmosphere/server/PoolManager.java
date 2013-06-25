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
	 * 
	 */
	private static final long serialVersionUID = -3509684187196223780L;

	private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

	private List<PoolItem> poolItems = new LinkedList<PoolItem>();

	private Map<String, IAgentManager> agentManagersId = new HashMap<String, IAgentManager>();

	private Map<IAgentManager, Registry> agentManagerRegistry = new HashMap<IAgentManager, Registry>();

	private Registry rmiRegistry;

	void onAgentDeviceListChanged(String onAgent)
	{
		if (agentManagersId.containsKey(onAgent) == false)
		{
			// TODO log this severe problem and return - we received an event from an agent with unregistered agentID;
			return;
		}

		IAgentManager agentManager = agentManagersId.get(onAgent);
		// TODO implement on device list change event handler
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
			rmiRegistry = LocateRegistry.createRegistry(rmiPort);
			String poolManagerRmiPublishString = com.musala.atmosphere.commons.cs.RmiStringConstants.POOL_MANAGER.toString();
			rmiRegistry.rebind(poolManagerRmiPublishString, this);
			LOGGER.info("PoolManager instance published in RMI (port " + rmiPort + ") under the identifier '"
					+ poolManagerRmiPublishString + "'.");
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
		IAgentManager agent = connectToAndRegisterAgent(ip, port);
		LOGGER.info("Connection to Agent with address [" + ip + ":" + port + "] established.");
		publishAllDeviceProxiesForAgent(agent);
	}

	private IAgentManager connectToAndRegisterAgent(String ip, int port) throws RemoteException, NotBoundException
	{
		Registry agentRegistry = LocateRegistry.getRegistry(ip, port);
		IAgentManager agent = (IAgentManager) agentRegistry.lookup(RmiStringConstants.AGENT_MANAGER.toString());
		agentManagersId.put(agent.getAgentId(), agent);
		agentManagerRegistry.put(agent, agentRegistry);
		// TODO register on the agent!
		// uncomment the following line when proper server IP fetching is implemented.
		// agent.registerServer(serverIPAddress, rmiRegistry.RMI_PORT);
		return agent;
	}

	private void publishAllDeviceProxiesForAgent(IAgentManager agent) throws RemoteException, NotBoundException
	{
		List<String> deviceWrappers = agent.getAllDeviceWrappers();
		Registry agentRegistry = agentManagerRegistry.get(agent);
		for (String wrapperRmiId : deviceWrappers)
		{
			IWrapDevice deviceWrapper = (IWrapDevice) agentRegistry.lookup(wrapperRmiId);
			publishDeviceProxy(deviceWrapper, agent);
		}
	}

	private void publishDeviceProxy(IWrapDevice deviceWrapper, IAgentManager onAgent) throws RemoteException
	{
		PoolItem poolItem = new PoolItem(deviceWrapper, onAgent, rmiRegistry);
		poolItems.add(poolItem);
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

}
