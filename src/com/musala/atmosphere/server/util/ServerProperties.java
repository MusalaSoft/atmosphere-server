package com.musala.atmosphere.server.util;

/**
 * Enumeration class containing all possible server properties.
 * 
 * @author valyo.yolovski
 * 
 */
public enum ServerProperties
{
	POOLMANAGER_PORT("poolmanager.port");

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
