package com.musala.atmosphere.server.util;

import com.musala.atmosphere.commons.PropertiesLoader;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Reads server properties from server properties config file.
 * 
 * @author valyo.yolovski
 * 
 */
public class ServerPropertiesLoader
{
	private static final String SERVER_PROPERTIES_FILE = "./server.properties";

	private synchronized static String getPropertyString(ServerProperties property)
	{
		PropertiesLoader propertiesLoader = PropertiesLoader.getInstance(SERVER_PROPERTIES_FILE);

		String propertyString = property.toString();

		String resultProperty = propertiesLoader.getPropertyString(propertyString);

		return resultProperty;
	}

	/**
	 * Returns the PoolManager RMI port from the server config file.
	 * 
	 * @return - the porn on which the {@link PoolManager} is published in RMI.
	 */
	public static int getPoolManagerRmiPort()
	{
		String returnValueString = getPropertyString(ServerProperties.POOL_MANAGER_RMI_PORT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}
}
