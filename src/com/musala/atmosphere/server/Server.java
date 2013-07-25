package com.musala.atmosphere.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

public class Server
{
	private static final Logger LOGGER = Logger.getLogger(Server.class.getCanonicalName());

	private static final List<Pair<String, Integer>> agentAddressesList = new LinkedList<Pair<String, Integer>>();

	private PoolManager poolManager;

	private int poolManagerPort;

	private volatile Thread serverThread;

	private ServerState serverState;

	private class InnerRunThread implements Runnable
	{
		/**
		 * Runs the Server.
		 */
		@Override
		public void run()
		{
			serverState = ServerState.SERVER_RUNNING;

			LOGGER.info("Running the server...");

			try
			{
				while (serverState == ServerState.SERVER_RUNNING)
				{
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e)
			{
				LOGGER.error("Something has interrupted the server thread.", e);
				Thread.currentThread().interrupt();
			}
			finally
			{
				stop();
			}
		}
	}

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
	 * @param serverRmiPort
	 *        - port on which the Pool Manager of the Server will be published in RMI.
	 * @throws RemoteException
	 */
	public Server(int serverRmiPort) throws RemoteException
	{
		serverState = ServerState.SERVER_CREATED;
		poolManagerPort = serverRmiPort;
		poolManager = new PoolManager(poolManagerPort);
		LOGGER.info("Server instance created succesfully on RMI port " + poolManagerPort);
	}

	/**
	 * Starts the Server thread.
	 * 
	 * @param blocking
	 *        true if this call should block while the Server is running, false otherwise.
	 */
	public void startServerThread(boolean blocking)
	{
		InnerRunThread innerThread = new InnerRunThread();
		serverThread = new Thread(innerThread, "ServerRunThread");
		serverThread.start();
		if (blocking)
		{
			try
			{
				serverThread.join();
			}
			catch (InterruptedException e)
			{
				LOGGER.warn("Failed to join with RunWait thread (blocking run).", e);
			}
		}
	}

	/**
	 * Stops the Server.
	 * 
	 * @throws InterruptedException
	 */
	public void stop()
	{
		if (serverState != ServerState.SERVER_STOPPED)
		{
			serverState = ServerState.SERVER_STOPPED;
			poolManager.close();
		}
		else
		{
			// TODO CHANGE to out.print when implemented.
			LOGGER.info("The server is already stopped.");
		}
	}

	// FIXME vlado should decide if this code is worth keeping
	//
	// /**
	// * Adds new Agent to the Server.
	// *
	// * @param agentIp
	// * - IP of the agent
	// * @param agentPort
	// * - number of Port, under which the agent is published in the RMI Registry.
	// */
	// public void addAgentToServer(String agentIp, int agentPort)
	// {
	// try
	// {
	// poolManager.connectToAgent(agentIp, agentPort);
	// agentAddressesList.add(new Pair<String, Integer>(agentIp, agentPort));
	// LOGGER.info("Added Agent on ip: \"" + agentIp + "\" on port: \"" + agentPort + "\" to the Server");
	// }
	// catch (RemoteException e)
	// {
	// LOGGER.error("Could not connect Server to Agent [" + agentIp + ":" + agentPort
	// + "]. Probably no Agent is created on given ip and port.", e);
	// }
	// catch (NotBoundException e)
	// {
	// LOGGER.error("You are trying to connect to something that is not Agent.", e);
	// }
	// }

	/**
	 * Creates emulator with given DeviceParameters on the least loaded emulator. <b><u>NOTE: This is beta version, so
	 * emulator will be created on the first Agent in the <b><i>agentAdressesList</i></b> on the Server.</u></b>
	 * 
	 * @param deviceParameters
	 *        - parameters of the requested emulator
	 * @throws NotBoundException
	 *         - when there is something on the RMI IP and Port, but it's not agent
	 * @throws IOException
	 *         - when there is problem while creating and starting emulator
	 */
	public void createEmulator(DeviceParameters deviceParameters) throws NotBoundException, IOException
	{
		int leastUsedAgent = findLeastUsedAgent();

		String agentIp = agentAddressesList.get(leastUsedAgent).getKey();
		int agentPort = agentAddressesList.get(leastUsedAgent).getValue();
		Registry agentRegistry = LocateRegistry.getRegistry(agentIp, agentPort);

		IAgentManager agent = (IAgentManager) agentRegistry.lookup(RmiStringConstants.AGENT_MANAGER.toString());

		com.musala.atmosphere.commons.sa.DeviceParameters wrappedDeviceParameters = new com.musala.atmosphere.commons.sa.DeviceParameters();
		wrappedDeviceParameters.setDpi(deviceParameters.getDpi());
		wrappedDeviceParameters.setRam(deviceParameters.getRam());
		int resolutionWidth = deviceParameters.getResolutionWidth();
		int resolutionHeight = deviceParameters.getResolutionHeight();
		wrappedDeviceParameters.setResolution(new Pair<Integer, Integer>(resolutionWidth, resolutionHeight));

		agent.createAndStartEmulator(wrappedDeviceParameters);
	}

	/**
	 * Creates emulator with given DeviceParameters on Agent with ip <b><i>agentIp</i></b> on port
	 * <b><i>agentRmiPort</i></b>. Exception is thrown, if no such agent exists. <b><i>NOTE: FOR NOW IT CAN ONLY CREATE
	 * EMULATORS ON LOCALHOST; IN ORDER TO CREATE EMULATORS ON OTHER AGENTS, A SYNCRONIZATION IN CONSTANTS FOR EMULATOR
	 * CONSOLE COMMANDS NEEDS TO BE DONE</i></b>
	 * 
	 * @param agentIp
	 *        - IP of the agent
	 * @param agentRmiPort
	 *        - number of port, under which the agent is published in RMI
	 * @param deviceParameters
	 *        - parameters of the requested emulator
	 * @throws NotBoundException
	 *         - when there is something on the RMI IP and Port, but it's not agent.
	 * @throws IOException
	 *         - when there is problem while creating and starting emulator.
	 */
	public void createEmulatorOnAgent(String agentIp, int agentRmiPort, DeviceParameters deviceParameters)
		throws NotBoundException,
			IOException
	{
		Registry agentRegistry = LocateRegistry.getRegistry(agentIp, agentRmiPort);
		IAgentManager agent = (IAgentManager) agentRegistry.lookup(RmiStringConstants.AGENT_MANAGER.toString());

		com.musala.atmosphere.commons.sa.DeviceParameters wrappedDeviceParameters = new com.musala.atmosphere.commons.sa.DeviceParameters();
		wrappedDeviceParameters.setDpi(deviceParameters.getDpi());
		wrappedDeviceParameters.setRam(deviceParameters.getRam());
		int resolutionWidth = deviceParameters.getResolutionWidth();
		int resolutionHeight = deviceParameters.getResolutionHeight();
		wrappedDeviceParameters.setResolution(new Pair<Integer, Integer>(resolutionWidth, resolutionHeight));

		LOGGER.info("Creating emulator on agent [" + agentIp + ":" + agentRmiPort + "]...");
		agent.createAndStartEmulator(wrappedDeviceParameters);
	}

	/**
	 * Gets all Agent's connection information - IPs and ports.
	 * 
	 * @return - List<> with Pairs of type <String,Integer> which stands for <ip, rmi port> for each Agent, attached to
	 *         the Server.
	 */
	public List<Pair<String, Integer>> getAgentAdressesList()
	{
		return agentAddressesList;
	}

	/**
	 * Gets all devices that are attached to its agents and return their device ID's.
	 * 
	 * @return - List of {@link DeviceProxy DeviceProxy} RMI binding identifiers.
	 */
	public List<String> getAllAccessibleDevices()
	{
		List<String> allDeviceIds = poolManager.getAllDeviceProxyIds();
		return allDeviceIds;
	}

	/**
	 * Gets the port under which the PoolManager of the Server is published in RMI. The Client should use this value as
	 * a port in his test class annotation.
	 * 
	 * @return - int, which is the Port, under which the Pool Manager is registered in RMI.
	 */
	public int getPoolManagerPort()
	{
		return poolManagerPort;
	}

	/**
	 * Finds with benchmarking the best-for-emulator-creation Agent , attached to this Server, and returns its index in
	 * the agentAdressesList.
	 * 
	 * @return - index of the least loaded with emulators Agent, or throws RuntimeException if no Agents are attached to
	 *         the Server.
	 */
	private int findLeastUsedAgent()
	{
		// FIXME genius
		if (agentAddressesList.isEmpty())
		{
			throw new RuntimeException("No available agents to create emulators on.");
		}
		return 0;
	}

	public static void main(String[] args) throws NotBoundException, IOException, InterruptedException
	{
		Server server = new Server();
		server.startServerThread(true); // blocking
		// server.stop();
	}

}