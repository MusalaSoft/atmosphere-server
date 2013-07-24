package com.musala.atmosphere.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

public class ServerMain
{
	private static final List<Pair<String, Integer>> agentAddressesList = new LinkedList<Pair<String, Integer>>();

	/**
	 * @param args
	 * @throws NotBoundException
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException, NotBoundException
	{
		PoolManager poolManager = new PoolManager(ServerPropertiesLoader.getPoolManagerPort());

		try
		{
			for (Pair<String, Integer> agentAddress : agentAddressesList)
			{
				String ip = agentAddress.getKey();
				int port = agentAddress.getValue();
				poolManager.connectToAgent(ip, port);
			}

			System.out.println("Server running...");
			while (true)
			{
				Thread.sleep(100);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Server closing.");
			poolManager.close();
		}
	}
}
