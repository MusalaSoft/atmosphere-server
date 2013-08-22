package com.musala.atmosphere.server.state;

import java.io.IOException;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.Server;

/**
 * Common class for all ServerStates.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class ServerState
{
	protected ConsoleControl serverConsole;

	protected Server server;

	public ServerState(Server server, ConsoleControl serverConsole)
	{
		this.server = server;
		this.serverConsole = serverConsole;
	}

	/**
	 * Writes string to the server's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeToConsole(String message)
	{
		serverConsole.write(message);
	}

	/**
	 * Writes line to the server's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeLineToConsole(String message)
	{
		serverConsole.writeLine(message);
	}

	/**
	 * Reads one line from the server's console.
	 * 
	 * @return - the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when an error occurs when trying to read from console
	 */
	public String readCommandFromConsole() throws IOException
	{
		String command = serverConsole.readCommand();
		return command;
	}

	// These methods are different for each server state.
	public abstract void run();

	public abstract void stop();

}
