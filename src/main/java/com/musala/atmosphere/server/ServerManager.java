package com.musala.atmosphere.server;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
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

    private Map<String, String> deviceSerialToDeviceId = new HashMap<>();

    private AgentAllocator agentAllocator = new AgentAllocator();

    private static final String CONNECT_TO_AGENT_LOGGER_FORMAT = "Connection to Agent with address %s : %d established.";

    // TODO: Remove
    private Registry rmiRegistry;

    private IDevicePoolDao devicePoolDao;

    private PoolManager poolManager = PoolManager.getInstance();

    private ServerEventService eventService = new ServerEventService();

    /**
     * Adds or removes a device with given ID from the agent's device list.
     *
     * @param agentId
     *        - the ID of the Agent
     * @param deviceSerial
     *        - the serial number of the device
     * @param deviceInformation
     *        - {@link DeviceInformation device information}
     * @param isConnected
     *        - this parameter is <code>true</code> if the device is connected to the agent and <code>false</code> if
     *        it's disconnected.
     */
    public void onAgentDeviceListChanged(String agentId,
                                         String deviceSerial,
                                         DeviceInformation deviceInformation,
                                         boolean isConnected)
                                                 throws CommandFailedException {
        if (!agentAllocator.hasAgent(agentId)) {
            // The agent which sends the event is not registered on server
            LOGGER.warn("Received device state change event from an Agent that is not registered on the server ("
                    + agentId + ").");
        } else {
            // The agent which sends the event is registered to the server
            if (isConnected) {
                String deviceId = poolManager.addDevice(deviceInformation, agentId);
                deviceSerialToDeviceId.put(deviceInformation.getSerialNumber(), deviceId);
            } else {
                String deviceId = deviceSerialToDeviceId.get(deviceSerial);
                try {
                    poolManager.removeDevice(deviceId);
                    deviceSerialToDeviceId.remove(deviceSerial);
                } catch (DevicePoolDaoException e) {
                    String errorMessage = String.format("Failed to remove device with ID %s.", deviceId);
                    LOGGER.error(errorMessage);
                }
            }
        }
    }

    /**
     * TODO: This method will never be called. Consider to remove it or write a calling logic on the Agent.
     *
     * Updates device's information.
     *
     * @param agentId
     *        - ID of the Agent to which the device is connected
     * @param deviceSerial
     *        - the unique identifier of the device
     */
    void onDeviceInformationChanged(String agentId, DeviceInformation deviceInformation) {
        if (!agentAllocator.hasAgent(agentId)) {
            // The agent which sends the event is not registered on server
            LOGGER.warn("Received device state change event from an Agent that is not registered on the server ("
                    + agentId + ").");
        } else {
            String deviceSerial = deviceInformation.getSerialNumber();
            String deviceId = deviceSerialToDeviceId.get(deviceSerial);

            poolManager.updateDevice(deviceId, deviceInformation);
        }
    }

    /**
     * Creates a new {@link ServerManager ServerManager} instance.
     *
     * TODO: The port param will be redundant her after the WS migration
     *
     * @param port
     *        port, on which the RMI registry for the new {@link ServerManager ServerManager} will be opened
     * @throws RemoteException
     *         - if failed to publish the {@link ServerManager} in the RMI registry
     */
    public ServerManager(int port) throws RemoteException {
        eventService.subscribe(AgentDaoCreatedEvent.class, agentAllocator);
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, poolManager);

        // TODO: Remove the logic below after the complete WebSocket migration
        try {
            rmiRegistry = LocateRegistry.createRegistry(port);

            String poolManagerRmiPublishString = com.musala.atmosphere.commons.cs.RmiStringConstants.POOL_MANAGER.toString();
            rmiRegistry.rebind(poolManagerRmiPublishString, poolManager);

            LOGGER.info("PoolManager instance published in RMI (port " + port + ") under the identifier '"
                    + poolManagerRmiPublishString + "'.");
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
     * TODO: Consider to remove or rewrite this method after the complete WebSocket migration.
     *
     * Closes all open resources. <b>MUST BE CALLED WHEN THIS CLASS IS NO LONGER NEEDED.</b>
     */
    public void close() {
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
     * Registers an Agent and adds it to the internal list of available agents to work with.
     *
     * @param agentId
     *        - the identifier of the Agent
     */
    public void registerAgent(String agentId) {
        try {
            agentAllocator.registerAgent(agentId);

            // Publish agent connected event to the event service.
            ServerEventService eventService = new ServerEventService();
            eventService.publish(new AgentConnectedEvent(agentId));
        } catch (AgentDaoException e) {
            String errorMessage = String.format("Failed to add agent with ID %s in the data source when trying to register the agent.",
                                                agentId);
            LOGGER.error(errorMessage, e);
            e.printStackTrace();
        }
    }

    /**
     * Publishes all devices for the connected agent.
     *
     * @param devicesInformation
     *        - the information for all agent's devices
     * @param agentId
     *        - the identifier of the connected agent
     */
    public void publishAllDevicesForAgent(DeviceInformation[] devicesInformation, String agentId) {
        for (DeviceInformation deviceInformation : devicesInformation) {
            String deviceSerial = deviceInformation.getSerialNumber();
            String deviceId = agentId + "_" + deviceSerial;
            deviceSerialToDeviceId.put(deviceSerial, deviceId);

            poolManager.addDevice(deviceInformation, agentId);
        }
    }

    /**
     * Gets the list of all connected Agent IDs.
     *
     * @return List&lt;String&gt; of Agent IDs.
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
     * TODO: Remove after complete WebSocket migration
     *
     * Gets the server's RMI registry.
     *
     * @return server's RMI registry
     */
    public Registry getRegistry() {
        return rmiRegistry;
    }

}
