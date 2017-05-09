package com.musala.atmosphere.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.message.ResponseMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;
import com.musala.atmosphere.commons.websocket.util.JsonConst;

/**
 * Represents a common endpoint for all incoming messages from the Agent. Handles the JSON message, resolves the type of
 * the message by the {@link MessageAction message action}, decides how to deserialize it and sends the
 * {@link RequestMessage request}/{@link ResponseMessage response} to the {@link ServerDispatcher dispatcher}.
 *
 * @author dimcho.nedev
 */
@ServerEndpoint("/server_agent")
public class ServerAgentEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ServerEndpoint.class.getCanonicalName());

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    private static final IJsonUtil jsonUtil = new GsonUtil();

    @OnMessage
    public void onJsonMessage(String jsonMessage, Session session) {
        // LOGGER.debug("Server: onMessage called");

        MessageAction messageAction = jsonUtil.getProperty(jsonMessage, JsonConst.MESSAGE_ACTION, MessageAction.class);

        switch (messageAction) {
            case REGISTER_AGENT:
                RequestMessage reqisterRequest = jsonUtil.deserializeRequest(jsonMessage);
                dispatcher.registerAgent(reqisterRequest, session);
                break;
            case DEVICE_CHANGED:
                RequestMessage deviceChangedRequest = jsonUtil.deserializeRequest(jsonMessage);
                dispatcher.deviceListChanged(deviceChangedRequest);
                break;
            case ROUTING_ACTION:
            case ERROR:
                dispatcher.sendToClient(jsonMessage);
                break;
            default:
                LOGGER.error(String.format("Unknown message action on the %s: %s",
                                           this.getClass().getSimpleName(),
                                           messageAction));
                break;
        }
    }

}
