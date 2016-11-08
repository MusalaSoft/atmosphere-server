package com.musala.atmosphere.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IConnectionRequestReceiver;
import com.musala.atmosphere.commons.util.Pair;

/**
 *
 * @author georgi.gaydarov
 *
 */
public class ConnectionRequestReceiver extends UnicastRemoteObject implements IConnectionRequestReceiver {

    /**
     * auto-generated serialization version ID
     */
    private static final long serialVersionUID = 7327169680500484452L;

    private static Logger LOGGER = Logger.getLogger(ConnectionRequestReceiver.class.getCanonicalName());

    private static final int CONNECTION_CYCLE_WAIT = 1000;

    private ServerManager commandedPool;

    private Thread connectingThread;

    private boolean connectingThreadRunFlag = true;

    private List<Pair<String, Integer>> connectionQueue = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new request receiver, that commands a passed {@link ServerManager ServerManager} to connect to a
     * remote Agent.
     *
     * @param poolToCommand
     *        {@link ServerManager ServerManager} instance that will be commanded.
     * @throws RemoteException
     *         - thrown when an error during the execution of a remote method call.
     */
    public ConnectionRequestReceiver(ServerManager poolToCommand) throws RemoteException {
        commandedPool = poolToCommand;
        connectingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (connectingThreadRunFlag) {
                    if (!connectionQueue.isEmpty()) {
                        Pair<String, Integer> address = connectionQueue.get(0);
                        String ip = address.getKey();
                        int port = address.getValue();
                        try {
                            commandedPool.connectToAgent(ip, port);
                        } catch (RemoteException | NotBoundException e) {
                            LOGGER.warn("Failed to establish connection to an Agent (" + ip + ":" + port
                                    + ") that requested to be connected to.", e);
                        }
                        connectionQueue.remove(address);
                    }
                    try {
                        Thread.sleep(CONNECTION_CYCLE_WAIT);
                    } catch (InterruptedException e) {
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
    public void close() {
        connectingThreadRunFlag = false;
    }

    @Override
    public void postConnectionRequest(int toPort) throws RemoteException {
        try {
            String invokerIp = RemoteServer.getClientHost();
            Pair<String, Integer> agentAddress = new Pair<>(invokerIp, toPort);
            connectionQueue.add(agentAddress);
            LOGGER.info("Received connection request to (" + invokerIp + ":" + toPort + ")");
        } catch (ServerNotActiveException e) {
            LOGGER.error("The local Java VM invoked postConnectionRequest(...).", e);
        }
    }

}
