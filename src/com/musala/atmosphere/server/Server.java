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
	 * Instantiates a Server object on loaded from config file port.
	 * 
	 * @throws RemoteException
	 */
	public Server() throws RemoteException
	{
		this(ServerPropertiesLoader.getPoolManagerPort());
	}

	/**
	 * Instantiates a Server object on given port.
	 * 
	 * @param serverPort
	 *        - port on which the Pool Manager of the Server will be published in RMI.
	 * @throws RemoteException
	 */
	public Server(int serverPort) throws RemoteException
	{
		serverManager = new ServerManager(serverRmiPort);
		currentServerState = new StoppedServer(this);

		serverConsole = new ConsoleControl();
		commandFactory = new ServerCommandFactory(this);

		serverRmiPort = serverPort;
		closed = false;
		LOGGER.info("Server instance created succesfully on RMI port " + serverRmiPort);
	}

	/**
	 * Sets the state of the server.
	 * 
	 * @param newState
	 */
	public void setState(ServerState newState)
	{
		this.currentServerState = newState;
	}

	/**
	 * Starts the Server thread, only if it is not already running.
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
	 * Releases all resources, used by the server and marks it as closed. After that the Server is no longer available
	 * and should be started again in order to be used.
	 */
	public void exit()
	{
		serverManager.close();
		closed = true;
	}

	/**
	 * Writes string to the server's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeToConsole(String message)
	{
		currentServerState.writeToConsole(message);
	}

	/**
	 * Writes line to the server's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeLineToConsole(String message)
	{
		currentServerState.writeLineToConsole(message);
	}

	/**
	 * Executes passed shell command from the user into the console of given Server.
	 * 
	 * @param passedShellCommand
	 *        - the shell command that the managing Server person wants to execute
	 * @throws IOException
	 */
	private void parseAndExecuteShellCommand(String passedShellCommand) throws IOException
	{
		if (passedShellCommand != null)
		{
			Pair<String, String[]> parsedCommand = ConsoleControl.parseShellCommand(passedShellCommand);
			String command = parsedCommand.getKey();
			String[] params = parsedCommand.getValue();

			if (!command.isEmpty())
			{
				executeShellCommand(command, params);
			}
		}
		else
		{
			LOGGER.error("Error in console: trying to execute 'null' as a command.");
			throw new IllegalArgumentException("Command passed to server is 'null'");
		}
	}

	/**
	 * Evaluates given command with the passed parameters.
	 * 
	 * @param commandName
	 * @param params
	 *        - arguments, passed to the command.
	 */
	private void executeShellCommand(String commandName, String[] params)
	{
		ServerConsoleCommands command = ServerConsoleCommands.findCommand(commandName);

		if (command == null)
		{
			currentServerState.writeLineToConsole("No such command. Type 'help' to retrieve list of available commands.");
			return;
		}

		ServerCommand executableCommand = commandFactory.getCommandInstance(command);
		executableCommand.execute(params);
	}

	/**
	 * Reads one line from the server's console. For more information see
	 * {@link com.musala.atmosphere.server.ServerConsole#readLine() ServerConsole.readLine()}
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

	/**
	 * 
	 * @return - true, if the server is killed, false otherwise.
	 */
	private boolean isClosed()
	{
		return closed;
	}

	public static void main(String[] args) throws NotBoundException, IOException, InterruptedException
	{
		// First we check if we have been passed an argument which specifies RMI port for the Server to be ran at.
		int portToCreateServerOn = ServerPropertiesLoader.getPoolManagerPort();
		if (args.length == 1)
		{
			String passedRmiPort = args[0];
			try
			{
				portToCreateServerOn = Integer.parseInt(passedRmiPort);
			}
			catch (NumberFormatException e)
			{
				LOGGER.warn("Error while trying to parse given port: argument is not a number.", e);
			}
		}

		// and then we create instance of the Server and run it
		Server localServer = new Server(portToCreateServerOn);
		localServer.run();
		do
		{
			String passedShellCommand = localServer.readCommandFromConsole();
			localServer.parseAndExecuteShellCommand(passedShellCommand);
		} while (!localServer.isClosed());
	}
}