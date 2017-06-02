package com.musala.atmosphere.server;

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
import com.musala.atmosphere.server.eventservice.event.device.publish.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;
import com.musala.atmosphere.server.pool.PoolManager;
import com.musala.atmosphere.server.websocket.ServerDispatcher;

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

    private IDevicePoolDao devicePoolDao;

    private PoolManager poolManager = PoolManager.getInstance();

    private ServerEventService eventService = new ServerEventService();

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    /**
     * Creates a new {@link ServerManager ServerManager} instance.
     *
     */
    public ServerManager() {
        eventService.subscribe(AgentDaoCreatedEvent.class, agentAllocator);
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, poolManager);
    }

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
                if(deviceId != null) {
                    deviceSerialToDeviceId.put(deviceInformation.getSerialNumber(), deviceId);
                }
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
     * Calls the {@link #close() close()} method just to be sure everything is closed.
     */
    @Override
    public void finalize() {
        close();
    }

    /**
     * Closes all open resources. <b>MUST BE CALLED WHEN THIS CLASS IS NO LONGER NEEDED.</b>
     *
     */
    public void close() {
        try {
            poolManager.removeAllDevices();
        } catch (DevicePoolDaoException | CommandFailedException e) {
            LOGGER.error("Failed to close a ServerManager instance.", e);
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

            LOGGER.info("Agent with id " + agentId + " registered.");
        } catch (AgentDaoException e) {
            String errorMessage = String.format("Failed to add agent with ID %s in the data source when trying to register the agent.",
                                                agentId);
            LOGGER.error(errorMessage, e);
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
            String deviceId = PoolManager.buildDeviceIdentifier(agentId, deviceSerial);
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
     * @throws DevicePoolDaoException
     *         - if devices for the disconnected agent could not be removed
     * @throws AgentDaoException
     *         - if the disconnected agent can not be removed
     */
    public void inform(AgentDisconnectedEvent event) throws DevicePoolDaoException, AgentDaoException {
        String agentId = event.getAgentId();
        devicePoolDao.removeDevices(agentId);
        agentAllocator.unregisterAgent(agentId);
        dispatcher.removeAgentSessionById(agentId);

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

    public void inform(DeviceUnpublishedEvent event) {
        String deviceId = PoolManager.buildDeviceIdentifier(event.getUnpublishDeviceOnAgentId(),
                                                            event.getUnpublishedDeviceSerialNumber());
        dispatcher.removeCachedSessionByDeviceId(deviceId);
    }

}
