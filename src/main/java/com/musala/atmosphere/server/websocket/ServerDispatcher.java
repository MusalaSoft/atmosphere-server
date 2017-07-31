package com.musala.atmosphere.server.websocket;

import static com.musala.atmosphere.commons.websocket.util.JsonConst.DEVICE_ID;
import static com.musala.atmosphere.commons.websocket.util.JsonConst.DEVICE_PASSKEY;
import static com.musala.atmosphere.commons.websocket.util.JsonConst.SESSION_ID;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.DeviceNotFoundException;
import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.message.ResponseMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;
import com.musala.atmosphere.server.PasskeyAuthority;
import com.musala.atmosphere.server.ServerManager;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Dispatches the {@link RequestMessage request} and {@link ResponseMessage response} messages. The ServerDispatcher is
 * also responsible for managing {@link Session agent sessions} and {@link Session client sessions}.
 *
 * @author dimcho.nedev
 *
 */
public class ServerDispatcher {
    private final Logger LOGGER = Logger.getLogger(ServerDispatcher.class.getCanonicalName());

    private ServerManager serverManager;

    private Map<String, Session> agentIdToAgentSessionCache = new ConcurrentHashMap<>();

    private Map<String, Session> deviceIdToClientSessionCache = new ConcurrentHashMap<>();

    private Map<String, Session> deviceIdToAgentSessionCache = new ConcurrentHashMap<>();

    private final IJsonUtil jsonUtil = new GsonUtil();

    private PoolManager poolManager = PoolManager.getInstance();

    private org.glassfish.tyrus.server.Server server;

    private ClientRequestMonitor timeoutMonitor = new ClientRequestMonitor();

    private static class DispatcherLoader {
        private static final ServerDispatcher INSTANCE = new ServerDispatcher();
    }

    public static ServerDispatcher getInstance() {
        return DispatcherLoader.INSTANCE;
    }

    /**
     * Starts the WebSocket server on the address and port from the config file.
     * 
     * @param serverAddress
     *        - an IP address for the WebSocket connection
     * @param websocketPort
     *        - a port for the WebSocket connection
     */
    public void startWebSocketServer(String serverAddress, int websocketPort) {
        server = new org.glassfish.tyrus.server.Server(serverAddress,
                                                       websocketPort,
                                                       null,
                                                       null,
                                                       ClientServerEndpoint.class,
                                                       ServerAgentEndpoint.class);
        LOGGER.info("Websocket Server started on port " + websocketPort);

        try {
            server.start();
        } catch (DeploymentException e) {
            LOGGER.error("Could not start WebSocket server.", e);
        }
    }

    /**
     * Stops the WebSocket server.
     */
    public void stopWebsocketServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Registers the connected agent on the server. Saves the agent's session for later use.
     *
     * @param registerAgentRequest
     *        - {@linkplain RequestMessage} request message for a registration
     * @param agentSession
     *        - the session of the agent
     */
    void registerAgent(RequestMessage registerAgentRequest, Session agentSession) {
        Object[] params = registerAgentRequest.getArguments();
        String agentId = (String) params[0];
        DeviceInformation[] devicesInformation = (DeviceInformation[]) params[1];

        agentIdToAgentSessionCache.put(agentId, agentSession);
        serverManager.registerAgent(agentId);

        serverManager.publishAllDevicesForAgent(devicesInformation, agentId);
    }

    /**
     * Gets called when something in the device list has changed.
     *
     * @param deviceListChangedRequest
     *        - the device list changed request message
     */
    void deviceListChanged(RequestMessage deviceListChangedRequest) {
        DeviceInformation deviceInformation = (DeviceInformation) deviceListChangedRequest.getArguments()[0];
        boolean isConnected = (boolean) deviceListChangedRequest.getArguments()[1];
        String agentId = deviceListChangedRequest.getAgentId();
        String deviceSerial = deviceListChangedRequest.getDeviceId();

        try {
            serverManager.onAgentDeviceListChanged(agentId, deviceSerial, deviceInformation, isConnected);
        } catch (CommandFailedException ex) {
            Session agentSession = agentIdToAgentSessionCache.get(agentId);
            sendErrorResponseMessage(ex, agentSession, deviceListChangedRequest.getSessionId());
            LOGGER.error(ex);
        }
    }

    /**
     * Routes the JSON request message to the Agent. Requests an action invocation on the device wrapper. Validate the
     * device passkey and caches the client {@link Session session} for later use. If an error occurs removes the client
     * session cache and sends an error response back to the client.
     *
     * @param jsonRequest
     *        - JSON request message
     * @param clientSession
     *        - client {@link Session session}
     */
    public void route(String jsonRequest, Session clientSession) {
        String deviceId = jsonUtil.getProperty(jsonRequest, DEVICE_ID, String.class);

        try {
            PasskeyAuthority.validatePasskey(jsonUtil.getProperty(jsonRequest, DEVICE_PASSKEY, Long.class), deviceId);
            if (!deviceIdToAgentSessionCache.containsKey(deviceId)) {
                String agentId = poolManager.getDeviceById(deviceId).getAgentId();
                Session agentSession = agentIdToAgentSessionCache.get(agentId);
                if (agentSession == null) {
                    // The case may occurs when the Agent is stopped
                    throw new CommandFailedException("Failed to send the request action to the device.");
                }

                deviceIdToAgentSessionCache.put(deviceId, agentSession);
            }

            deviceIdToClientSessionCache.put(deviceId, clientSession);
            Session agentSession = deviceIdToAgentSessionCache.get(deviceId);
            timeoutMonitor.restartTimerForDevice(deviceId);

            sendText(jsonRequest, agentSession);
        } catch (InvalidPasskeyException | DeviceNotFoundException | CommandFailedException ex) {
            String sessionId = jsonUtil.getProperty(jsonRequest, SESSION_ID, String.class);
            sendErrorResponseMessage(ex, clientSession, sessionId);
            deviceIdToClientSessionCache.remove(deviceId);
            LOGGER.error(ex);
        }
    }

    /**
     * Sends a JSON message to a client.
     *
     * @param json
     *        - JSON message
     */
    void sendToClient(String json) {
        String deviceId = jsonUtil.getProperty(json, DEVICE_ID, String.class);
        Session clientSession = deviceIdToClientSessionCache.get(deviceId);

        sendText(json, clientSession);
    }

    /**
     * Allocates a device and send back a message with the {@link DeviceAllocationInformation} to the Client.
     *
     * @param getDeviceAllocationInformationRequest
     *        - {@link RequestMessage request message}
     * @param clientSession
     *        - the client's {@link Session session}
     */
    void sendGetDeviceAllocationInfoRequest(RequestMessage getDeviceAllocationInformationRequest,
                                            Session clientSession) {

        DeviceSelector deviceSelector = (DeviceSelector) getDeviceAllocationInformationRequest.getArguments()[0];

        try {
            DeviceAllocationInformation deviceAllocationInformation = poolManager.allocateDevice(deviceSelector);
            ResponseMessage response = new ResponseMessage(MessageAction.DEVICE_ALLOCATION_INFORMATION,
                                                           deviceAllocationInformation);
            response.setSessionId(getDeviceAllocationInformationRequest.getSessionId());

            sendText(jsonUtil.serialize(response), clientSession);
        } catch (NoDeviceMatchingTheGivenSelectorException | NoAvailableDeviceFoundException ex) {
            sendErrorResponseMessage(ex, clientSession, getDeviceAllocationInformationRequest.getSessionId());
            LOGGER.error(ex);
        }

    }

    /**
     * Returns a list with serial numbers and models of all available devices.
     *
     * @return a list with serial numbers and models of available devices
     */
    void sendGetAllAvailableDevicesRequest(RequestMessage getAllAvailableDevicesRequres, Session clientSession) {
        List<Pair<String, String>> devices = poolManager.getAllAvailableDevices();
        ResponseMessage response = new ResponseMessage(MessageAction.GET_ALL_AVAILABLE_DEVICES, devices);
        response.setSessionId(getAllAvailableDevicesRequres.getSessionId());

        sendText(jsonUtil.serialize(response), clientSession);
    }

    /**
     * Releases an allocated device. Removes all cached sessions associated to the device.
     *
     * @param deviceDescriptor
     *        - the {@link DeviceAllocationInformation} corresponding to the device which should be released
     */
    void releaseDevice(RequestMessage requestMessage, Session session) {
        DeviceAllocationInformation deviceDescriptor = (DeviceAllocationInformation) requestMessage.getArguments()[0];

        String deviceId = deviceDescriptor.getDeviceId();
        removeCachedSessionByDeviceId(deviceId);

        try {
            poolManager.releaseDevice(deviceDescriptor);
            ResponseMessage releseResponse = new ResponseMessage(MessageAction.RELEASE_DEVICE, null, null);
            releseResponse.setSessionId(requestMessage.getSessionId());

            sendText(jsonUtil.serialize(releseResponse), session);
        } catch (InvalidPasskeyException | DeviceNotFoundException ex) {
            sendErrorResponseMessage(ex, session, requestMessage.getSessionId());
            LOGGER.error("Failed to release a device with id " + deviceId, ex);
        }
    }

    /**
     * Sends a ping request to a specific agent. The ping has not default message handler on the Server's endpoint and
     * will be received implicitly.
     *
     * @param agentId
     *        - the identifier of agent
     * @throws IllegalStateException
     *         - if the connection has been closed from the Agent
     * @throws IOException
     *         - thrown when an I/O exception of some sort has occurred during sending the request
     */
    public void sendPing(String agentId) throws IllegalStateException, IOException {
        Session agentSession = agentIdToAgentSessionCache.get(agentId);
        agentSession.getBasicRemote().sendPing(null);
    }

    /**
     * Sends a pong request to a specific agent. The pong message handler will indicate that the request is received on
     * the Server.
     *
     * @param agentId
     *        - the identifier of agent
     * @throws IllegalStateException
     *         - if the connection has been closed from the Agent
     * @throws IOException
     *         - thrown when an I/O exception of some sort has occurred during sending the request
     */
    public void sendPong(String agentId) throws IllegalStateException, IOException {
        Session agentSession = agentIdToAgentSessionCache.get(agentId);
        agentSession.getBasicRemote().sendPong(null);
    }

    /**
     * Maps the agent Id to a session.
     *
     * @param agentId
     *        - the Id of the Agent
     * @param agentSession
     *        - the {@link javax.websocket.Session} session of the Agent
     */
    public void addAgentSession(String agentId, Session agentSession) {
        agentIdToAgentSessionCache.put(agentId, agentSession);
    }

    /**
     * Removes an agent's session from the cache by an identifier.
     *
     * @param agentId
     *        - the identifier of the agent to remove
     */
    public void removeAgentSessionById(String agentId) {
        Session agentSession = agentIdToAgentSessionCache.remove(agentId);
        deviceIdToAgentSessionCache.values().remove(agentSession);
    }

    /**
     * Removes all cached {@link Session sessions} associated with a particular device identifier. Used when a device is
     * unpublished or released.
     *
     * @param deviceId
     *        - the identifier of the device
     */
    public void removeCachedSessionByDeviceId(String deviceId) {
        deviceIdToAgentSessionCache.remove(deviceId);
        deviceIdToClientSessionCache.remove(deviceId);
    }

    /**
     * Sets a {@link ServerManager server manager} to the dispatcher.
     *
     * @param serverManager
     */
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    private void sendErrorResponseMessage(Exception ex, Session session, String requestSessionId) {
        ResponseMessage errorResponse = new ResponseMessage(MessageAction.ERROR, null, null);
        errorResponse.setSessionId(requestSessionId);
        errorResponse.setException(ex);

        sendText(jsonUtil.serialize(errorResponse), session);
    }

    private void sendText(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.error("Failed to send a JSON message.", e);
        }
    }

}
