package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Runs the server. This command requires no parameters to be passed, otherwise it will not be executed.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class RunCommand extends NoParamsServerCommand
{
	public RunCommand(Server server)
	{
		super(server);
	}

	@Override
	public void executeCommand(String[] params)
	{
		server.run();
	}
}
