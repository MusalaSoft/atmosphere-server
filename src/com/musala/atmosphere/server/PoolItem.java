package com.musala.atmosphere.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

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

	private Registry serverRmiRegistry;

	/**
	 * Creates a new {@link PoolItem PoolItem} object that wraps a device wrapper in a {@link DeviceProxy DeviceProxy}
	 * object and publishes it on the server's RMI registry.
	 * 
	 * @param deviceWrapper
	 *        device to be wrapped in a {@link DeviceProxy DeviceProxy} object.
	 * @param onAgent
	 *        the AgentManager that published the {@link IWrapDevice IWrapDevice} we are wrapping.
	 * @param serverRmiRegistry
	 *        RMI registry in which we will publish the newly created {@link DeviceProxy DeviceProxy} wrapper.
	 * @throws RemoteException
	 */
	public PoolItem(IWrapDevice deviceWrapper, IAgentManager onAgent, Registry serverRmiRegistry)
		throws RemoteException
	{
		this.onAgent = onAgent;
		deviceProxy = new DeviceProxy(deviceWrapper);
		deviceInformation = deviceWrapper.getDeviceInformation();
		this.serverRmiRegistry = serverRmiRegistry;

		deviceProxyRmiString = buildDeviceProxyRmiBindingIdentifier();
		serverRmiRegistry.rebind(deviceProxyRmiString, deviceProxy);
		LOGGER.info("DeviceProxy instance published in the RMI registry under the identifier '" + deviceProxyRmiString
				+ "'");
	}

	/**
	 * Unbinds the underlying {@link DeviceProxy DeviceProxy} from the RMI registry.
	 * 
	 * @throws RemoteException
	 */
	public void unbindDeviceProxyFromRmi() throws RemoteException
	{
		try
		{
			serverRmiRegistry.unbind(deviceProxyRmiString);
		}
		catch (NotBoundException e)
		{
			// The device proxy was never registered anyway, so unbinding is 'done'.
			// nothing to do here.
			e.printStackTrace();
		}
		LOGGER.info("DeviceProxy with string identifier '" + deviceProxyRmiString + "' unbound from the RMI registry");
	}

	private String buildDeviceProxyRmiBindingIdentifier() throws RemoteException
	{
		String rmiIdentifier = onAgent.getAgentId() + " " + deviceInformation.getSerialNumber();
		return rmiIdentifier;
	}

	/**
	 * @return the string under which the underlying {@link DeviceProxy DeviceProxy} was registered in the RMI registry.
	 */
	public String getDeviceProxyRmiBindingIdentifier()
	{
		return deviceProxyRmiString;
	}
}
