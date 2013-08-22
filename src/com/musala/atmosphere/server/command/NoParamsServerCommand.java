package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Common class for all servers which require no parameters. The logic for the verification is the same for all of them.
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
		else
		{
			return true;
		}
	}
}
