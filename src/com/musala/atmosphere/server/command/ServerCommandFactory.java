package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * A factory which is instantiated with an server associated to it. For a given ServerConsoleCommand enum value it
 * returns the proper instance of command which should be used by this server.
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
	 * @return - instance of ServerCommand which is associated with the passed consoleCommand.
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
