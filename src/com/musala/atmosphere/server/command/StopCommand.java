package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * This class executes the "stop" command. After execution of this command the Server can not be run again from the
 * console with the "run" command and needs to be started again. This command requires no parameters to be passed,
 * otherwise it will not be executed.
 */
public class StopCommand extends NoParamsServerCommand
{
	public StopCommand(Server server)
	{
		super(server);
	}

	/**
	 * Stops the Server thread if it's running.
	 */
	@Override
	protected void executeCommand(String[] params)
	{
		server.stop();
	}
}
