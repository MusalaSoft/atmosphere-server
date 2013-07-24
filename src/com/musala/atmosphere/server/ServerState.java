package com.musala.atmosphere.server;

/**
 * Enumerates all server states - instantiated but not running, running and stopped.
 * 
 * @author vladimir.vladimirov
 * 
 */
public enum ServerState
{
	SERVER_CREATED("server_created"), SERVER_RUNNING("server_running"), SERVER_STOPPED("server_stopped");

	private String value;

	private ServerState(String stateOfserver)
	{
		this.value = stateOfserver;
	}

	public String toString()
	{
		return value;
	}
}
