package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Common class for console commands, executed by the server.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class ServerCommand
{
	private static final String ILLEGAL_ARGUMENTS_MESSAGE = "Illegal arguments for command. "
			+ "Use command 'help' for more information.";

	protected Server server;

	public ServerCommand(Server server)
	{
		this.server = server;
	}

	public void execute(String[] params)
	{
		if (!verifyParams(params))
		{
			server.writeLineToConsole(ILLEGAL_ARGUMENTS_MESSAGE);
		}
		else
		{
			executeCommand(params);
		}
	}

	// Those methods are different for each command.
	protected abstract boolean verifyParams(String[] params);

	protected abstract void executeCommand(String[] params);
}
