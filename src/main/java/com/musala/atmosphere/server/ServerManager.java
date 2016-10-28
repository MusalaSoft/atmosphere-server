package com.musala.atmosphere.server;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.agent.AgentConnectedEvent;
import com.musala.atmosphere.server.eventservice.event.agent.AgentDisconnectedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Class that is responsible for managing device selection/distribution.
 *
 * @author georgi.gaydarov
 *
 */
public class ServerManager implements Subscriber {
    private static Logger LOGGER = Logger.getLogger(ServerManager.class.getCanonicalName());

    private Map<String, String> rmiIdToDeviceId = new HashMap<String, String>();

    private AgentAllocator agentAllocator = new AgentAllocator();

    private static final String CONNECT_TO_AGENT_LOGGER_FORMAT = "Connection to Agent with address %s : %d established.";

    private int rmiRegistryPort;

    private Registry rmiRegistry;

    private IDevicePoolDao devicePoolDao;

    private AgentEventSender agentChangeNotifier;

    private ConnectionRequestReceiver connectionRequestReceiver;

    private PoolManager poolManager = PoolManager.getInstance();

    private ServerEventService eventService = new ServerEventService();

    /**
     * Adds or removes a device with given ID from the agent's device list.
     *
     * @param onAgent
     *        - the ID of the Agent
     * @param changedDeviceRmiId
     *        - the unique RMI identifier of the device
     * @param isConnected
     *        - this parameter is <code>true</code> if the device is connected to the agent and <code>false</code> if
     *        it's disconnected.
     * @throws RemoteException
     *         - if fails to remove the device
     * @throws NotBoundException
     *         - if an attempt is made to remove non-existing device
     */
    void onAgentDeviceListChanged(String onAgent, String changedDeviceRmiId, boolean isConnected)
            throws RemoteException,
            CommandFailedException,
            NotBoundException {
        if (!agentAllocator.hasAgent(onAgent)) {
            // The agent which sends the event is not registered on server
            LOGGER.warn("Received device state change event from an Agent that is not registered on the server ("
                    + onAgent + ").");
        } else {
            // The agent which sends the event is registered to the server
            if (isConnected) {
                Registry agentRegistry = agentAllocator.getAgentRegistry(onAgent);
                String deviceId = poolManager.addDevice(changedDeviceRmiId, agentRegistry, onAgent);
                rmiIdToDeviceId.put(changedDeviceRmiId, deviceId);
            } else {
                String deviceId = rmiIdToDeviceId.get(changedDeviceRmiId);
                try {
                    poolManager.removeDevice(deviceId);
                    rmiIdToDeviceId.remove(changedDeviceRmiId);
                } catch (DevicePoolDaoException e) {
                    String errorMessage = String.format("Failed to remove device with ID %s.", deviceId);
                    LOGGER.error(errorMessage);
                }
            }
        }
    }

    /**
     * Updates device's information.
     * 
     * @param agentId
     *        - ID of the Agent to which the device is connected
     * @param changedDeviceRmiId
     *        - the unique RMI identifier of the device
     */
    void onDeviceInformationChanged(String agentId, String changedDeviceRmiId) {
        if (!agentAllocator.hasAgent(agentId)) {
            // The agent which sends the event is not registered on server
            LOGGER.warn("Received device state change event from an Agent that is not registered on the server ("
                    + agentId + ").");
        } else {
            String deviceToUpdateId = rmiIdToDeviceId.get(changedDeviceRmiId);
            Registry agentRegistry = agentAllocator.getAgentRegistry(agentId);
            poolManager.updateDevice(changedDeviceRmiId, deviceToUpdateId, agentRegistry);
        }
    }

    /**
     * Creates a new {@link ServerManager ServerManager} instance that opens an RMI registry on a specific port and
     * waits for a client connection.
     *
     * @param rmiPort
     *        port, on which the RMI registry for the new {@link ServerManager ServerManager} will be opened
     * @throws RemoteException
     *         - if failed to publish the {@link ServerManager} in the RMI registry
     */
    public ServerManager(int rmiPort) throws RemoteException {
        eventService.subscribe(AgentDaoCreatedEvent.class, agentAllocator);
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, poolManager);

        // Publish this ServerManager in the RMI registry
        try {
            // TODO: Extract this logic to the RemoteObjectregistryManager
            rmiRegistryPort = rmiPort;
            rmiRegistry = LocateRegistry.createRegistry(rmiPort);

            String poolManagerRmiPublishString = com.musala.atmosphere.commons.cs.RmiStringConstants.POOL_MANAGER.toString();
            rmiRegistry.rebind(poolManagerRmiPublishString, poolManager);

            LOGGER.info("PoolManager instance published in RMI (port " + rmiPort + ") under the identifier '"
                    + poolManagerRmiPublishString + "'.");
            String connectionRequestReceiverRmiString = RmiStringConstants.CONNECTION_REQUEST_RECEIVER.toString();
            connectionRequestReceiver = new ConnectionRequestReceiver(this);
            rmiRegistry.rebind(connectionRequestReceiverRmiString, connectionRequestReceiver);

            LOGGER.info("Connection request receiver instance published in RMI under the identifier '"
                    + connectionRequestReceiverRmiString + "'.");
        } catch (RemoteException e) {
            close();
            throw e;
        }

        // Publish an AgentEventSender in the RMI registry
        try {
            agentChangeNotifier = new AgentEventSender(this);
            String agentChangeNotifierRmiPublishString = RmiStringConstants.AGENT_EVENT_SENDER.toString();

            rmiRegistry.rebind(agentChangeNotifierRmiPublishString, agentChangeNotifier);
            LOGGER.info("AgentEventSender instance published in RMI (port " + rmiPort + ") under the identifier '"
                    + agentChangeNotifierRmiPublishString + "'.");
        } catch (RemoteException e) {
            close();
            throw e;
        }
    }

    /**
     * Calls the {@link #close() close()} method just to be sure everything is closed.
     */
    @Override
    public void finalize() {
        close();
    }

    /**
     * Closes all open resources. <b>MUST BE CALLED WHEN THIS CLASS IS NO LONGER NEEDED.</b>
     */
    public void close() {
        if (connectionRequestReceiver != null) {
            connectionRequestReceiver.close();
            connectionRequestReceiver = null;
        }
        try {
            // Close the registry
            if (rmiRegistry != null) {
                // unexport the poolItems one by one
                poolManager.removeAllDevices();

                // unexport everything else
                String[] rmiObjectIds = rmiRegistry.list();
                for (String rmiObjectId : rmiObjectIds) {
                    Object obj = rmiRegistry.lookup(rmiObjectId);
                    try {
                        rmiRegistry.unbind(rmiObjectId);
                        UnicastRemoteObject.unexportObject((Remote) obj, true);
                    } catch (NoSuchObjectException e) {
                        LOGGER.warn("No such object.", e);
                    }
                }
            }
            UnicastRemoteObject.unexportObject(rmiRegistry, true);
        } catch (Exception e) {
            // If something cannot be closed it was never opened, so it's okay.
            // Nothing to do here.
            e.printStackTrace();
        }
        LOGGER.info("ServerManager instance closed.");
    }

    /**
     * Connects to an Agent and adds it to the internal list of available agents to work with.
     *
     * @param ip
     *        address of the agent we want to connect to.
     * @param port
     *        port that the agent has created it's registry on.
     * @throws RemoteException
     *         - if the agent registry could not be contacted
     * @throws NotBoundException
     *         - if looking for agent that is not in the registry
     */
    public void connectToAgent(String ip, int port) throws RemoteException, NotBoundException {
        String agentId = connectToAndRegisterAgent(ip, port);

        String message = String.format(CONNECT_TO_AGENT_LOGGER_FORMAT, ip, port);
        LOGGER.info(message);
        publishAllDeviceProxiesForAgent(agentId);
    }

    private String connectToAndRegisterAgent(String ip, int port) throws RemoteException, NotBoundException {
        // Get the agent rmi stub
        Registry agentRegistry = LocateRegistry.getRegistry(ip, port);
        IAgentManager agent = (IAgentManager) agentRegistry.lookup(RmiStringConstants.AGENT_MANAGER.toString());
        String agentId = agent.getAgentId();

        // Add the agent stub to the agent lists
        try {
            agentAllocator.registerAgent(agent, agentRegistry, ip, port);
        } catch (AgentDaoException e) {
            String errorMessage = String.format("Failed to add agent with ID %s in the data source when trying to register the agent.",
                                                agentId);
            LOGGER.error(errorMessage, e);
        }

        // Register the server for event notifications
        String serverIpForAgent = agent.getInvokerIpAddress();
        agent.registerServer(serverIpForAgent, rmiRegistryPort);

        // Publish agent connected event to the event service.
        AgentConnectedEvent agentConnectedEvent = new AgentConnectedEvent(agent, agentRegistry);
        eventService.publish(agentConnectedEvent);

        return agentId;
    }

    private void publishAllDeviceProxiesForAgent(String agentId) throws RemoteException, NotBoundException {
        IAgentManager agentManager = agentAllocator.getAgentManager(agentId);
        List<String> deviceWrappers = agentManager.getAllDeviceRmiIdentifiers();
        Registry agentRegistry = agentAllocator.getAgentRegistry(agentId);

        for (String wrapperRmiId : deviceWrappers) {
            String deviceId = poolManager.addDevice(wrapperRmiId, agentRegistry, agentId);
            rmiIdToDeviceId.put(wrapperRmiId, deviceId);
        }
    }

    /**
     * Gets the list of all connected Agent IDs.
     *
     * @return List<String> of Agent IDs.
     */
    public List<String> getAllConnectedAgentIds() {
        return agentAllocator.getAllConnectedAgentsIds();
    }

    /**
     * Informs server manager for {@link AgentDisconnectedEvent event} received when an agent disconnects.
     *
     * @param event
     *        - event, which is received when an agent is disconnected
     * @throws RemoteException
     *         - if could not get the agent ID
     * @throws DevicePoolDaoException
     *         - if devices for the disconnected agent could not be removed
     * @throws AgentDaoException
     *         - if the disconnected agent can not be removed
     */

    public void inform(AgentDisconnectedEvent event) throws RemoteException, DevicePoolDaoException, AgentDaoException {
        String agentId = event.getAgentId();
        devicePoolDao.removeDevices(agentId);
        agentAllocator.unregisterAgent(agentId);

        String debugMessage = String.format("Unregister agent with ID %s from server and remove all devices attached on it",
                                            agentId);
        LOGGER.debug(debugMessage);
    }

    /**
     * Informs server manager for {@link AgentConnectedEvent event} received when an agent connects.
     *
     * @param event
     *        - event, which is received when an agent is connected
     */
    public void inform(AgentConnectedEvent event) {
        // TODO: Re-factor agent allocator to use events on agent connected.

        LOGGER.debug("An agent connected event is received.");
    }

    public void inform(DevicePoolDaoCreatedEvent event) {
        IDataSourceProvider dataSoureceProvider = new DataSourceProvider();
        devicePoolDao = dataSoureceProvider.getDevicePoolDao();
    }

    /**
     * Gets the server's RMI registry.
     *
     * @return server's RMI registry
     */
    public Registry getRegistry() {
        return rmiRegistry;
    }
}
