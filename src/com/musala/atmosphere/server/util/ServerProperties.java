package com.musala.atmosphere.server.util;

/**
 * Enumeration class containing all possible server properties.
 * 
 * @author valyo.yolovski
 * 
 */
public enum ServerProperties
{
	POOL_MANAGER_RMI_PORT("pool.manager.rmi.port"),
	DEVICE_REQUEST_TIMEOUT("device.request.timeout"),
	DEVICE_UPDATE_TIME("device.update.timeout");

	private String value;

	private ServerProperties(String value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return value;
	}
}
