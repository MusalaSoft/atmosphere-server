package com.musala.atmosphere.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.websocket.WebSocketCommunicatorManager;
import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.message.ResponseMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;

/**
 * Represents a common endpoint for all incoming messages.
 *
 * @author dimcho.nedev
 */
@ServerEndpoint("/server_agent")
public class ServerAgentEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ServerEndpoint.class.getCanonicalName());

    private ServerDispatcher websocketCommunicator = ServerDispatcher.getInstance();

    private WebSocketCommunicatorManager communicatorManager = WebSocketCommunicatorManager.getInstance();

    private static final IJsonUtil jsonUtil = new GsonUtil();

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.debug("Server: onOpen");
    }

    @OnMessage
    public void onJsonMessage(String json, Session session) {
        LOGGER.debug("Server: onMessage called");

        MessageAction messageAction = (MessageAction) jsonUtil.getProperty(json, "messageAction", MessageAction.class);

        switch (messageAction) {
            case REGISTER_AGENT:
                RequestMessage reqisterRequest = jsonUtil.deserializeRequest(json);
                websocketCommunicator.registerAgent(reqisterRequest, session);
                break;
            case DEVICE_CHANGED:
                RequestMessage deviceChangedRequest = jsonUtil.deserializeRequest(json);
                websocketCommunicator.deviceListChanged(deviceChangedRequest);
                break;
            case ROUTING_ACTION:
                ResponseMessage response = jsonUtil.deserializeResponse(json);
                communicatorManager.setResponse(response);
                break;
            default:
                LOGGER.error("Unknown message action");
                break;
        }
    }

}
