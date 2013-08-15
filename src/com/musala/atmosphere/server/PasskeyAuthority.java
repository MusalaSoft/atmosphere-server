package com.musala.atmosphere.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.cs.InvalidPasskeyException;

/**
 * A {@link DeviceProxy DeviceProxy} passkey validating authority.
 * 
 * @author georgi.gaydarov
 * 
 */
public class PasskeyAuthority
{
	private final static Logger LOGGER = Logger.getLogger(PasskeyAuthority.class.getCanonicalName());

	private Map<DeviceProxy, Long> proxyKeys = new HashMap<DeviceProxy, Long>();

	private static PasskeyAuthority authorityInstance = new PasskeyAuthority();

	private final Random generator;

	private PasskeyAuthority()
	{
		generator = new Random();
		LOGGER.info("PasskeyAuthority instance created.");
	}

	/**
	 * Gets the underlying class instance.
	 * 
	 * @return {@link PasskeyAuthority PasskeyAuthority} instance.
	 */
	public static PasskeyAuthority getInstance()
	{
		return authorityInstance;
	}

	/**
	 * Checks if a given passkey is valid in a {@link DeviceProxy DeviceProxy} context.
	 * 
	 * @param proxy
	 *        - the {@link DeviceProxy DeviceProxy} context.
	 * @param passkey
	 *        - the passkey to be validated.
	 */
	public void validatePasskey(DeviceProxy proxy, long passkey) throws InvalidPasskeyException
	{
		long actualProxyPasskey = proxyKeys.get(proxy);
		if (passkey != actualProxyPasskey)
		{
			throw new InvalidPasskeyException("The passed passkey is not valid in the specified DeviceProxy context.");
		}
	}

	/**
	 * Gets the passkey for a specified {@link DeviceProxy DeviceProxy} instance.
	 * 
	 * @param proxy
	 *        - the {@link DeviceProxy DeviceProxy} to get the passkey for.
	 * @return - the required passkey.
	 */
	public long getPasskey(DeviceProxy proxy)
	{
		if (!proxyKeys.containsKey(proxy))
		{
			renewPasskey(proxy);
		}
		long proxyPasskey = proxyKeys.get(proxy);
		return proxyPasskey;
	}

	/**
	 * Renews the passkey for a specified {@link DeviceProxy DeviceProxy} instance.
	 * 
	 * @param proxy
	 *        - the {@link DeviceProxy DeviceProxy} to change the passkey for.
	 */
	public void renewPasskey(DeviceProxy proxy)
	{
		long oldKey = 0;
		if (proxyKeys.containsKey(proxy))
		{
			oldKey = getPasskey(proxy);
		}
		long newKey = 0;
		do
		{
			long initialRandomValue = generator.nextLong();
			Random seededGenerator = new Random(initialRandomValue);
			long secondRandomValue = seededGenerator.nextLong();
			newKey = initialRandomValue ^ secondRandomValue;
		} while (newKey == oldKey);

		proxyKeys.put(proxy, newKey);
	}

	/**
	 * Removes the passkey for specified {@link DeviceProxy DeviceProxy} from the internal passkey map.
	 * 
	 * @param proxy
	 *        - the {@link DeviceProxy DeviceProxy} for which to remove the passkey.
	 */
	public void removeDevice(DeviceProxy proxy)
	{
		if (proxyKeys.containsKey(proxy))
		{
			proxyKeys.remove(proxy);
		}
	}
}
