package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * A {@link ServerCommand ServerCommand} factory. Builds a {@link ServerCommand ServerCommand} For a given
 * {@link ServerConsoleCommands ServerConsoleCommands} enumeration value.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class ServerCommandFactory
{
	private final Server server;

	public ServerCommandFactory(Server server)
	{
		this.server = server;
	}

	/**
	 * 
	 * @param consoleCommand
	 * @return {@link ServerCommand ServerCommand} instance associated with the passed consoleCommand.
	 */
	public ServerCommand getCommandInstance(ServerConsoleCommands consoleCommand)
	{
		ServerCommand resultCommand = null;
		switch (consoleCommand)
		{
			case SERVER_RUN:
			{
				resultCommand = new RunCommand(server);
				break;
			}
			case SERVER_HELP:
			{
				resultCommand = new HelpCommand(server);
				break;
			}
			case SERVER_STOP:
			{
				resultCommand = new StopCommand(server);
				break;
			}
		}

		return resultCommand;
	}
}
