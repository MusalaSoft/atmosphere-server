package com.musala.atmosphere.server.state;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.Server;

/**
 * This class represents stopped state of the server.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class StoppedServer extends ServerState
{
	private static final String SERVER_NOT_RUNNING_MESSAGE = "Could not stop server: server not running.";

	public StoppedServer(Server server, ConsoleControl serverConsole)
	{
		super(server, serverConsole);
	}

	public StoppedServer(Server server)
	{
		this(server, new ConsoleControl());
	}

	/**
	 * Runs the server.
	 */
	@Override
	public void run()
	{
		server.setState(new RunningServer(server, serverConsole));
	}

	/**
	 * The server can be stopped only once and calling this method from StoppedState of the Server means we want to
	 * close it again.
	 */
	@Override
	public void stop()
	{
		server.writeLineToConsole(SERVER_NOT_RUNNING_MESSAGE);
	}

}
