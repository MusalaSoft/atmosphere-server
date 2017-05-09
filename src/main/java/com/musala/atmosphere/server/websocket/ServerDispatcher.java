package com.musala.atmosphere.server.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.websocket.WebSocketCommunicatorManager;
import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.message.ResponseMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;
import com.musala.atmosphere.server.ServerManager;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.pool.PoolManager;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

/**
 * Dispatches and sends the {@link RequestMessage request} and {@link ResponseMessage response} messages.
 *
 * @author dimcho.nedev
 *
 */
public class ServerDispatcher {
    private static final Logger LOGGER = Logger.getLogger(ServerDispatcher.class.getCanonicalName());

    private static final int WAIT_FOR_RESPONSE_MAX_TIMEOUT = 30_000;

    private static ServerDispatcher instance = null;

    private ServerManager serverManager;

    private Map<String, Session> agentIdToAgentSessionMap = new ConcurrentHashMap<>();;

    private static WebSocketCommunicatorManager communicationManager = WebSocketCommunicatorManager.getInstance();

    private static final IJsonUtil jsonUtil = new GsonUtil();

    private PoolManager poolManager = PoolManager.getInstance();

    private org.glassfish.tyrus.server.Server server;

    private ServerDispatcher() {
    }

    public static ServerDispatcher getInstance() {
        if (instance == null) {
            synchronized (ServerDispatcher.class) {
                if (instance == null) {
                    LOGGER.info("Creating new WebSocketCommunicator instance...");
                    instance = new ServerDispatcher();
                }
            }
        }

        return instance;
    }

    /**
     * Starts the WebSocket server on the address and port from the config file.
     */
    public void startWebSocketServer() {
        ServerDispatcher.getInstance().setServerManager(serverManager);
        String serverAddress = ServerPropertiesLoader.getServerIp();
        int websocketPort = ServerPropertiesLoader.getWebSocketPort();

        server = new org.glassfish.tyrus.server.Server(serverAddress,
                                                       websocketPort,
                                                       null,
                                                       null,
                                                       ServerAgentEndpoint.class,
                                                       ClientServerEndpoint.class);
        LOGGER.info("Websocket Server started on port " + websocketPort);

        try {
            server.start();
        } catch (DeploymentException e) {
            LOGGER.error("Could not start WebSocket server.");
        }
    }

    /**
     * Stops the WebSocket server.
     */
    public void stopWebsocketServer() {
        server.stop();
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

        agentIdToAgentSessionMap.put(agentId, agentSession);
        serverManager.registerAgent(agentId);

        LOGGER.debug("<<< " + agentId + " Session added >>>");

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
            sendErrorResponseMessageToAgent(ex, agentId);
            ex.printStackTrace();
        }
    }

    private void sendErrorResponseMessageToAgent(Exception ex, String agentId) {
        ResponseMessage errorResponse = new ResponseMessage(MessageAction.ERROR, null, null);
        errorResponse.setException(ex);
        Session agentSession = agentIdToAgentSessionMap.get(agentId);
        String jsonResponseError = jsonUtil.serialize(errorResponse);
        try {
            agentSession.getBasicRemote().sendText(jsonResponseError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a routing action.
     *
     * @param deviceId
     *        - the id of the device
     * @param action
     *        - {@link RoutingAction routing action}
     * @param args
     *        - the arguments of the action
     * @return the result of the action as a Java {@link Object}
     */
    public Object executeRoute(String deviceId, RoutingAction action, Object... args) {
        RequestMessage webSocketRequest = new RequestMessage(MessageAction.ROUTING_ACTION, action, args);
        webSocketRequest.setDeviceId(deviceId);

        Object returnValue = null;
        try {
            returnValue = sendJsonRequest(webSocketRequest, deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * Sends a JSON request message to a specific device.
     *
     * @param webSocketRequest
     *        - the {@link RequestMessage request message} to a specific device
     * @param deviceId
     *        - the id of the device
     * @return the result of the request as a Java {@link Object}
     * @throws Exception
     *         - thrown when the {@link ResponseMessage response message} has an exception property
     */
    private Object sendJsonRequest(RequestMessage webSocketRequest, String deviceId) throws Exception {
        IDevice device = poolManager.getDeviceById(deviceId);
        String agentId = device.getAgentId();
        Session agentSession = this.agentIdToAgentSessionMap.get(agentId);

        webSocketRequest.setSessionId(agentSession.getId());

        LOGGER.debug("<<< Agent Session: " + agentSession + " >>>");

        ResponseMessage webSocketResponse = sendRequestForResponse(webSocketRequest, agentSession);
        Object response = webSocketResponse.getData();

        if (webSocketResponse.getException() != null) {
            throw webSocketResponse.getException();
        }

        return response;
    }

    private ResponseMessage sendRequestForResponse(RequestMessage request, Session session) throws IOException {
        String sessionId = request.getSessionId();
        Object lockObject = communicationManager.getSynchronizationObject(sessionId);

        String requestJSON = jsonUtil.serialize(request);

        session.getBasicRemote().sendText(requestJSON);
        LOGGER.info("Sending request:");
        LOGGER.info(requestJSON);

        LOGGER.info("Waiting for response.");
        synchronized (lockObject) {
            try {
                lockObject.wait(WAIT_FOR_RESPONSE_MAX_TIMEOUT);
            } catch (InterruptedException e) {
                LOGGER.info("Waiting interrupted.");
            }
        }

        LOGGER.info("Getting the response...");

        return communicationManager.getResponse(sessionId);
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
        Session agentSession = agentIdToAgentSessionMap.get(agentId);
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
        Session agentSession = agentIdToAgentSessionMap.get(agentId);
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
    public void addAgent(String agentId, Session agentSession) {
        LOGGER.debug("<<< " + agentId + " Session added >>>");
        agentIdToAgentSessionMap.put(agentId, agentSession);
    }

    /**
     * Removes an agent's session by identifier.
     *
     * @param agentId
     *        - the identifier of the agent to remove
     */
    public void removeAgent(String agentId) {
        agentIdToAgentSessionMap.remove(agentId);
    }

    /**
     * Sets a {@link ServerManager server manager} to the dispatcher
     *
     * @param serverManager
     */
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

}
