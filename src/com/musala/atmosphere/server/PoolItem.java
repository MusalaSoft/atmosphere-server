package com.musala.atmosphere.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.DeviceInformation;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;

/**
 * The {@link PoolItem PoolItem} object is a managing container for a {@link DeviceProxy DeviceProxy} instance. It is
 * used by the {@link PoolManager PoolManager} as a pooling object.
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

	private final IAgentManager onAgent;

	private final String onAgentId;

	private final String deviceWrapperAgentRmiId;

	private int serverRmiRegistryPort;

	/**
	 * Creates a new {@link PoolItem PoolItem} object that wraps a device wrapper in a {@link DeviceProxy DeviceProxy}
	 * object and publishes it on the server's RMI registry.
	 * 
	 * @param deviceWrapperId
	 *        RMI string identifier on the Agent for the device wrapper stub.
	 * @param deviceWrapper
	 *        device to be wrapped in a {@link DeviceProxy DeviceProxy} object.
	 * @param onAgent
	 *        the {@link AgentManager AgentManager} that published the {@link IWrapDevice IWrapDevice} we are wrapping.
	 * @param serverRmiRegistry
	 *        RMI registry in which we will publish the newly created {@link DeviceProxy DeviceProxy} wrapper.
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
				+ "'");
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
		}
		catch (NotBoundException e)
		{
			// The device proxy was never registered anyway, so unbinding is 'done'.
			// nothing to do here.
			e.printStackTrace();
		}
		catch (RemoteException e)
		{
			LOGGER.warn("Attempting to unbind a DeviceProxy resulted in a RemoteException.", e);
			return;
		}
		catch (MalformedURLException e)
		{
			LOGGER.warn("Unbinding DeviceProxy with id " + deviceProxyRmiString + " failed..", e);
			return;
		}

		LOGGER.info("DeviceProxy with string identifier '" + deviceProxyRmiString + "' unbound from the RMI registry");
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

	public boolean isUnderlyingDeviceWrapperAsArguments(String onAgentId, String deviceProxyId)
	{
		boolean returnValue = onAgentId.equals(this.onAgentId) && deviceProxyId.equals(deviceWrapperAgentRmiId);
		return returnValue;
	}
}
