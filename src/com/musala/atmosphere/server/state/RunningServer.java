package com.musala.atmosphere.server.state;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.Server;

/**
 * This class represents the running state of the Server. It creates a new inner thread for the Server class which
 * prevents the Server from closing until explicitly stated something else.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class RunningServer extends ServerState
{
	private static final Logger LOGGER = Logger.getLogger(RunningServer.class.getCanonicalName());

	private static final String SERVER_ALREADY_RUNNING_ERROR = "Cannot run server: server already running.";

	private Thread serverThread;

	private boolean isRunning;

	public RunningServer(Server server, ConsoleControl serverConsole)
	{
		super(server, serverConsole);
		InnerRunThread innerThread = new InnerRunThread();
		serverThread = new Thread(innerThread, "ServerRunningWaitThread");
		serverThread.start();

		isRunning = true;
	}

	/**
	 * The methods prints the user a warning logger message explaining that the server is in running state and cannot be
	 * run again.
	 */
	@Override
	public void run()
	{
		LOGGER.warn(SERVER_ALREADY_RUNNING_ERROR);
	}

	/**
	 * Stops the server and kills its running threads.
	 */
	@Override
	public void stop()
	{
		server.setState(new StoppedServer(server, serverConsole));
		server.exit();
		isRunning = false;
	}

	private class InnerRunThread implements Runnable
	{
		@Override
		public void run()
		{
			LOGGER.info("Running Server...");
			try
			{
				// we must make sure the server thread is not stopped until we say so
				while (isRunning)
				{
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e)
			{
				LOGGER.warn("Something has interrupted the current thread.", e);

				// if our thread was interrupted, then tear down the whole Server
				stop();
				return;
			}
		}
	}
}
