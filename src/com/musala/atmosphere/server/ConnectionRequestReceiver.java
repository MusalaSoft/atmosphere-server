package com.musala.atmosphere.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.IConnectionRequestReceiver;

/**
 * 
 * @author georgi.gaydarov
 * 
 */
public class ConnectionRequestReceiver extends UnicastRemoteObject implements IConnectionRequestReceiver
{

	/**
	 * auto-generated serialization version ID
	 */
	private static final long serialVersionUID = 7327169680500484452L;

	private static Logger LOGGER = Logger.getLogger(ConnectionRequestReceiver.class.getCanonicalName());

	private static final int CONNECTION_CYCLE_WAIT = 1000;

	private PoolManager commandedPool;

	private Thread connectingThread;

	private boolean connectingThreadRunFlag = true;

	private List<Pair<String, Integer>> connectionQueue = new CopyOnWriteArrayList<Pair<String, Integer>>();

	/**
	 * Constructs a new request receiver, that commands a passed {@link PoolManager PoolManager} to connect to a remote
	 * Agent.
	 * 
	 * @param poolToCommand
	 *        {@link PoolManager PoolManager} instance that will be commanded.
	 * @throws RemoteException
	 */
	public ConnectionRequestReceiver(PoolManager poolToCommand) throws RemoteException
	{
		commandedPool = poolToCommand;
		connectingThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (connectingThreadRunFlag)
				{
					if (!connectionQueue.isEmpty())
					{
						Pair<String, Integer> address = connectionQueue.get(0);
						String ip = address.getKey();
						int port = address.getValue();
						try
						{
							commandedPool.connectToAgent(ip, port);
						}
						catch (RemoteException | NotBoundException e)
						{
							LOGGER.warn("Failed to establish connection to an Agent (" + ip + ":" + port
									+ ") that requested to be connected to.", e);
						}
						connectionQueue.remove(address);
					}
					try
					{
						Thread.sleep(CONNECTION_CYCLE_WAIT);
					}
					catch (InterruptedException e)
					{
						// Sleep interrupted, no one cares, nothing to do here.
						e.printStackTrace();
					}
				}
			}
		});
		connectingThread.start();
	}

	/**
	 * Shuts down the current request receiver.
	 */
	public void close()
	{
		connectingThreadRunFlag = false;
	}

	@Override
	public void postConnectionRequest(int toPort) throws RemoteException
	{
		try
		{
			String invokerIp = RemoteServer.getClientHost();
			Pair<String, Integer> agentAddress = new Pair<String, Integer>(invokerIp, toPort);
			connectionQueue.add(agentAddress);
			LOGGER.info("Received connection request to (" + invokerIp + ":" + toPort + ")");
		}
		catch (ServerNotActiveException e)
		{
			LOGGER.error("The local Java VM invoed postConnectionRequest(...).", e);
		}

	}

}