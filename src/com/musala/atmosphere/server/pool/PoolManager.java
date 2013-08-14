package com.musala.atmosphere.server.pool;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.IClientBuilder;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.ServerManager;
import com.musala.atmosphere.server.util.DeviceMatchingComparator;

public class PoolManager extends UnicastRemoteObject implements IClientBuilder
{
	/**
	 * auto-generated serialization id
	 */
	private static final long serialVersionUID = -5077918124351182199L;

	private static Logger LOGGER = Logger.getLogger(PoolManager.class.getCanonicalName());

	private static HashMap<ServerManager, PoolManager> serverToPoolManagerInstances = new HashMap<ServerManager, PoolManager>();

	private List<PoolItem> poolItems = new LinkedList<PoolItem>();

	private PoolManager() throws RemoteException
	{
	}

	/**
	 * Creates an instance (or gets the current if such instance already exists) of the {@link PoolManager PoolManager}.
	 * 
	 * @param serverManager
	 *        - the ServerManager for which we want to create/get an instance of PoolManager.
	 * @return PoolManager Instance
	 */
	public static PoolManager getInstance(ServerManager serverManager)
	{
		// FIXME Make this thread safe.
		PoolManager poolManagerInstance = serverToPoolManagerInstances.get(serverManager);
		if (poolManagerInstance == null)
		{
			try
			{
				PoolManager poolManager = new PoolManager();
				serverToPoolManagerInstances.put(serverManager, poolManager);
				return poolManager;
			}
			catch (RemoteException e)
			{
				// This is never thrown really, it's just that UnicastRemoteObject requires the constructor to throw a
				// RemoteException.
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return poolManagerInstance;
		}
	}

	/**
	 * Checks whether a device is present in the pool.
	 * 
	 * @param changedDeviceRmiId
	 *        - RMI string identifier for the device.
	 * @param agent
	 *        - the agent that the device is connected to.
	 * @return True if the device is present, and false if it is not.
	 */
	public boolean isDevicePresent(String changedDeviceRmiId, String agent)
	{
		for (PoolItem poolItem : poolItems)
		{
			if (poolItem.isCorrespondingTo(agent, changedDeviceRmiId))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Refreshes the current state of the device - removes the device from the pool if it is not present on an Agent
	 * anymore.
	 * 
	 * @param changedDeviceRmiId
	 *        - RMI string identifier on the Agent for the device wrapper stub.
	 * @param agent
	 *        - the {@link AgentManager AgentManager} that the device is connected to.
	 * @param isConnected
	 *        - true if the device is connected to an Agent, false if not.
	 */
	public void refreshDevice(String changedDeviceRmiId, String agent, boolean isConnected)
	{
		for (PoolItem poolItem : poolItems)
		{
			if (!poolItem.isCorrespondingTo(agent, changedDeviceRmiId))
			{
				continue;
			}
			if (isConnected)
			{
				LOGGER.warn("Received device connected event for a device that is already registered.");
			}
			else
			{
				LOGGER.info("PoolItem disconnected and removed.");
				poolItem.unbindDeviceProxyFromRmi();
				poolItems.remove(poolItem);
				return;
			}
		}
		LOGGER.warn("Received refresh request for a device that is not present in the pool.");
	}

	/**
	 * Unexports all devices from the pool.
	 */
	public void unexportAllPoolItems()
	{
		for (PoolItem poolItem : poolItems)
		{
			poolItem.unbindDeviceProxyFromRmi();
		}
	}

	/**
	 * Adds a device to the pool.
	 * 
	 * @param deviceRmiId
	 *        - RMI string identifier on the Agent for the device wrapper stub.
	 * @param deviceWrapper
	 *        - device to be wrapped in a {@link DeviceProxy DeviceProxy} object.
	 * @param agent
	 *        - the {@link AgentManager AgentManager} that published the {@link IWrapDevice IWrapDevice} we are
	 *        wrapping.
	 * @param serverRmiRegistry
	 *        - RMI registry in which we will publish the newly created {@link DeviceProxy DeviceProxy} wrapper.
	 */
	public void addDevice(String deviceRmiId, IWrapDevice deviceWrapper, IAgentManager agent, int rmiRegistryPort)
	{
		try
		{
			PoolItem poolItem = new PoolItem(deviceRmiId, deviceWrapper, agent, rmiRegistryPort);
			poolItems.add(poolItem);
			LOGGER.info("PoolItem created with rmi id: " + deviceRmiId + ".");
		}
		catch (RemoteException e)
		{
			LOGGER.error("PoolItem creation failed.", e);
		}
	}

	@Override
	public synchronized String getDeviceProxyRmiId(DeviceParameters deviceParameters) throws RemoteException
	{
		Map<DeviceInformation, PoolItem> freePoolItemsDeviceInfoMap = new HashMap<DeviceInformation, PoolItem>();
		List<DeviceInformation> freePoolItemsDeviceInfoList = new ArrayList<DeviceInformation>();
		for (PoolItem poolItem : poolItems)
		{
			if (poolItem.isAvailable())
			{
				DeviceInformation poolItemDeviceInformation = poolItem.getUnderlyingDeviceInformation();

				if (DeviceMatchingComparator.isValidMatch(deviceParameters, poolItemDeviceInformation) == false)
				{
					continue;
				}

				freePoolItemsDeviceInfoMap.put(poolItemDeviceInformation, poolItem);
				freePoolItemsDeviceInfoList.add(poolItemDeviceInformation);
			}
		}

		if (freePoolItemsDeviceInfoList.size() == 0)
		{
			// TODO add logic behind device creation
			LOGGER.warn("No available devices found.");
		}
		DeviceMatchingComparator matchComparator = new DeviceMatchingComparator(deviceParameters);
		DeviceInformation bestMatchDeviceInformation = Collections.max(freePoolItemsDeviceInfoList, matchComparator);
		PoolItem bestMatchPoolItem = freePoolItemsDeviceInfoMap.get(bestMatchDeviceInformation);
		String bestMatchDeviceProxyRmiId = bestMatchPoolItem.getDeviceProxyRmiBindingIdentifier();

		bestMatchPoolItem.setAvailability(false);
		return bestMatchDeviceProxyRmiId;
	}

	/**
	 * Gets the list of all published {@link DeviceProxy DeviceProxy} instance IDs.
	 * 
	 * @return List<String> of all device proxy IDs preswent in the device pool.
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

	@Override
	public void releaseDevice(String rmiId) throws RemoteException
	{

		for (PoolItem poolItem : poolItems)
		{
			if (poolItem.getDeviceProxyRmiBindingIdentifier().equals(rmiId))
			{
				poolItem.setAvailability(true);
				LOGGER.info("Released device with rmi id " + rmiId);
				break;
			}
		}
	}

}
