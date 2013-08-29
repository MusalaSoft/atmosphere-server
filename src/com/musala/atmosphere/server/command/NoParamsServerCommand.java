package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Common class for all server commands that require no parameters.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class NoParamsServerCommand extends ServerCommand
{
	public NoParamsServerCommand(Server server)
	{
		super(server);
	}

	@Override
	protected boolean verifyParams(String[] params)
	{
		if (params != null && params.length != 0)
		{
			return false;
		}
		return true;
	}
}
