package com.musala.atmosphere.server.pool;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.ServerManager;

/**
 * The {@link PoolItem PoolItem} object is a managing container for a {@link DeviceProxy DeviceProxy} instance. It is
 * used by the {@link ServerManager PoolManager} as a pooling object.
 * 
 * @author georgi.gaydarov
 * 
 */
public class PoolItem
{
	private static Logger LOGGER = Logger.getLogger(PoolItem.class.getCanonicalName());

	private DeviceProxy deviceProxy;

	private String deviceProxyRmiString;

	private DeviceInformation deviceInformation;

	private boolean availability = true; // Device is available on creation.

	private final IAgentManager onAgent;

	private final String onAgentId;

	private final String deviceWrapperAgentRmiId;

	private int serverRmiRegistryPort;

	/**
	 * Creates a new {@link PoolItem PoolItem} object that wraps a device wrapper in a {@link DeviceProxy DeviceProxy}
	 * object and publishes it on the server's RMI registry.
	 * 
	 * @param deviceWrapperId
	 *        - RMI string identifier on the Agent for the device wrapper stub.
	 * @param deviceWrapper
	 *        - device to be wrapped in a {@link DeviceProxy DeviceProxy} object.
	 * @param onAgent
	 *        - the {@link AgentManager AgentManager} that published the {@link IWrapDevice IWrapDevice} we are
	 *        wrapping.
	 * @param serverRmiRegistry
	 *        - RMI registry in which we will publish the newly created {@link DeviceProxy DeviceProxy} wrapper.
	 * @throws RemoteException
	 */
	public PoolItem(String deviceWrapperId, IWrapDevice deviceWrapper, IAgentManager onAgent, int serverRmiRegistryPort)
		throws RemoteException
	{
		deviceWrapperAgentRmiId = deviceWrapperId;
		this.onAgent = onAgent;
		onAgentId = onAgent.getAgentId();
		deviceProxy = new DeviceProxy(deviceWrapper);

		deviceInformation = deviceWrapper.getDeviceInformation();

		this.serverRmiRegistryPort = serverRmiRegistryPort;

		deviceProxyRmiString = buildDeviceProxyRmiBindingIdentifier();

		try
		{
			Naming.rebind("//localhost:" + serverRmiRegistryPort + "/" + deviceProxyRmiString, deviceProxy);
		}
		catch (MalformedURLException e)
		{
			throw new RemoteException(	"Exception occured when rebinding the device wrapper. See the enclosed exception.",
										e);
		}

		LOGGER.info("DeviceProxy instance published in the RMI registry under the identifier '" + deviceProxyRmiString
				+ "'.");
	}

	/**
	 * Unbinds the underlying {@link DeviceProxy DeviceProxy} from the RMI registry.
	 * 
	 * @throws RemoteException
	 */
	public void unbindDeviceProxyFromRmi()
	{
		try
		{
			Naming.unbind("//localhost:" + serverRmiRegistryPort + "/" + deviceProxyRmiString);
			UnicastRemoteObject.unexportObject(deviceProxy, true);
			LOGGER.info("DeviceProxy with string identifier '" + deviceProxyRmiString
					+ "' unbound from the RMI registry.");
		}
		catch (NotBoundException e)
		{
			// The device proxy was never registered anyway, so unbinding is 'done'.
			// nothing to do here.
			LOGGER.warn("Device proxy not bound to be unbound.", e);
		}
		catch (NoSuchObjectException e)
		{
			LOGGER.warn("Failed to unexport already unexported Remote object. Probably Agent was closed before the Server.",
						e);
		}
		catch (RemoteException e)
		{
			LOGGER.warn("Attempting to unbind a DeviceProxy resulted in a RemoteException.", e);
		}
		catch (MalformedURLException e)
		{
			LOGGER.warn("Unbinding DeviceProxy with id " + deviceProxyRmiString + " failed.", e);
		}
	}

	private String buildDeviceProxyRmiBindingIdentifier()
	{
		String rmiIdentifier = onAgentId + "_" + deviceWrapperAgentRmiId;
		return rmiIdentifier;
	}

	/**
	 * @return the string under which the underlying {@link DeviceProxy DeviceProxy} was registered in the RMI registry.
	 */
	public String getDeviceProxyRmiBindingIdentifier()
	{
		return deviceProxyRmiString;
	}

	public DeviceInformation getUnderlyingDeviceInformation()
	{
		return deviceInformation;
	}

	/**
	 * Checks whether the current device is corresponding to an actual device on the agent.
	 * 
	 * @param agentId
	 *        - the agent on which the real device is plugged.
	 * @param deviceProxyId
	 *        - the proxy id of the device connected on the agent.
	 * @return True if the device actually exists on the agent, false if not.
	 */
	public boolean isCorrespondingTo(String agentId, String deviceProxyId)
	{
		boolean isCurrentDeviceOnTheSameAgent = agentId.equals(onAgentId);
		boolean isTheSameDeviceProxy = deviceProxyId.equals(deviceWrapperAgentRmiId);

		boolean isCorresponding = isCurrentDeviceOnTheSameAgent && isTheSameDeviceProxy;

		return isCorresponding;
	}

	/**
	 * Check whether a device is available for allocation to a Client.
	 * 
	 * @return - True if the device is available in the pool, false if the device is already allocated.
	 */
	public boolean isAvailable()
	{
		return availability;
	}

	void setAvailability(boolean availability)
	{
		this.availability = availability;
	}

}
