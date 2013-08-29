package com.musala.atmosphere.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.command.ServerCommand;
import com.musala.atmosphere.server.command.ServerCommandFactory;
import com.musala.atmosphere.server.command.ServerConsoleCommands;
import com.musala.atmosphere.server.pool.PoolManager;
import com.musala.atmosphere.server.state.ServerState;
import com.musala.atmosphere.server.state.StoppedServer;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

public class Server
{

	private static final Logger LOGGER = Logger.getLogger(Server.class.getCanonicalName());

	private ServerManager serverManager;

	private ConsoleControl serverConsole;

	private ServerCommandFactory commandFactory;

	private ServerState currentServerState;

	private int serverRmiPort;

	private boolean closed;

	/**
	 * Creates a Server component bound on specified in the properties file port.
	 * 
	 * @throws RemoteException
	 */
	public Server() throws RemoteException
	{
		this(ServerPropertiesLoader.getPoolManagerPort());
	}

	/**
	 * Creates a Server component bound on given port.
	 * 
	 * @param serverPort
	 *        - port on which the Pool Manager of the Server will be published in RMI.
	 * @throws RemoteException
	 */
	public Server(int serverPort) throws RemoteException
	{
		serverManager = new ServerManager(serverRmiPort);
		setState(new StoppedServer(this));

		serverConsole = new ConsoleControl();
		commandFactory = new ServerCommandFactory(this);

		serverRmiPort = serverPort;
		closed = false;
		LOGGER.info("Server instance created succesfully.");
	}

	/**
	 * Sets the server state.
	 * 
	 * @param newState
	 */
	public void setState(ServerState newState)
	{
		currentServerState = newState;
	}

	/**
	 * Starts the Server thread if it is not already running.
	 */
	public void run()
	{
		currentServerState.run();
	}

	/**
	 * Stops the Server if it's running.
	 */
	public void stop()
	{
		currentServerState.stop();
	}

	/**
	 * Releases all resources used by the server and marks it as closed. After that the Server is no longer available
	 * and should be started again in order to be used.
	 */
	public void exit()
	{
		serverManager.close();
		closed = true;
	}

	/**
	 * Prints a string to the Server's console output.
	 * 
	 * @param message
	 *        - the message to be printed.
	 */
	public void writeToConsole(String message)
	{
		currentServerState.writeToConsole(message);
	}

	/**
	 * Prints a line to the Server's console output.
	 * 
	 * @param message
	 *        - the message to be printed.
	 */
	public void writeLineToConsole(String message)
	{
		currentServerState.writeLineToConsole(message);
	}

	/**
	 * Executes a passed command from the console.
	 * 
	 * @param passedShellCommand
	 *        - the passed shell command.
	 * @throws IOException
	 */
	private void parseAndExecuteShellCommand(String passedShellCommand) throws IOException
	{
		if (passedShellCommand == null)
		{
			throw new IllegalArgumentException("Shell command passed for execution can not be 'null'.");
		}

		Pair<String, String[]> parsedCommand = ConsoleControl.parseShellCommand(passedShellCommand);
		String command = parsedCommand.getKey();
		String[] params = parsedCommand.getValue();

		if (!command.isEmpty())
		{
			executeShellCommand(command, params);
		}
	}

	/**
	 * Evaluates a passed command and calls the appropriate Server method.
	 * 
	 * @param commandName
	 *        - command for execution.
	 * @param params
	 *        - passed command arguments.
	 */
	private void executeShellCommand(String commandName, String[] params)
	{
		ServerConsoleCommands command = ServerConsoleCommands.findCommand(commandName);

		if (command == null)
		{
			currentServerState.writeLineToConsole("Unknown command. Use 'help' to retrieve list of available commands.");
			return;
		}

		ServerCommand executableCommand = commandFactory.getCommandInstance(command);
		executableCommand.execute(params);
	}

	/**
	 * Reads one line from the server's console. For more information see
	 * {@link com.musala.atmosphere.server.ServerConsole#readLine() ServerConsole.readLine()}
	 * 
	 * @return the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when a console reading error occurs.
	 */
	public String readCommandFromConsole() throws IOException
	{
		String command = serverConsole.readCommand();
		return command;
	}

	/**
	 * 
	 * @return true if the server is closed, false otherwise.
	 */
	private boolean isClosed()
	{
		return closed;
	}

	public static void main(String[] args) throws NotBoundException, IOException, InterruptedException
	{
		// Check if an argument which specifies a port for the Server was passed.
		int portToCreateServerOn = 0;
		try
		{
			if (args.length == 1)
			{
				String passedRmiPort = args[0];
				portToCreateServerOn = Integer.parseInt(passedRmiPort);
			}
			else
			{
				portToCreateServerOn = ServerPropertiesLoader.getPoolManagerPort();
			}
		}
		catch (NumberFormatException e)
		{
			String errorMessage = "Parsing passed port resulted in an exception.";
			LOGGER.fatal(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}

		Server localServer = new Server(portToCreateServerOn);
		localServer.run();
		do
		{
			String passedShellCommand = localServer.readCommandFromConsole();
			localServer.parseAndExecuteShellCommand(passedShellCommand);
		} while (!localServer.isClosed());
	}
}