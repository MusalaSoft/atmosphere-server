package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Class that specifies the "stop" command behavior.
 * 
 * @author vladimir.vladimirov
 */
public class StopCommand extends NoParamsServerCommand
{
	public StopCommand(Server server)
	{
		super(server);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		server.stop();
	}
}
