package com.musala.atmosphere.server.util;

import com.musala.atmosphere.commons.PropertiesLoader;

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
	 * Returns the Pool Manager Port from the server config file.
	 * 
	 * @return
	 */
	public static int getPoolManagerPort()
	{
		String returnValueString = getPropertyString(ServerProperties.POOLMANAGER_PORT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}
}
