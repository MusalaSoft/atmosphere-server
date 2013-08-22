package com.musala.atmosphere.server.command;

import java.util.List;

import com.musala.atmosphere.server.Server;

/**
 * This class executes the "help" command. It prints on the standart output information about all available commands on
 * the Server. The command requires no parameters to be passed, otherwise it will not be executed.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class HelpCommand extends NoParamsServerCommand
{
	public HelpCommand(Server server)
	{
		super(server);
	}

	/**
	 * Prints information about every single command on the Server that can be executed.
	 */
	@Override
	protected void executeCommand(String[] params)
	{
		List<String> listOfCommands = ServerConsoleCommands.getListOfCommands();
		if (listOfCommands != null)
		{
			for (String serverConsoleCommand : listOfCommands)
			{
				server.writeLineToConsole(serverConsoleCommand);
			}
		}
	}
}
