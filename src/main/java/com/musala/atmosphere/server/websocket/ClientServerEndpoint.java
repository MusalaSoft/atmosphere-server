package com.musala.atmosphere.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;

/**
 * TODO: The class is not implemented yet. It is a part of the Client-Server WebSocket migration stage.
 *
 * Represents an endpoint of the client-server communication.
 *
 * @author dimcho.nedev
 *
 */
@ServerEndpoint("/client_server")
public class ClientServerEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ClientServerEndpoint.class.getCanonicalName());

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    private static final IJsonUtil jsonUtil = new GsonUtil();

    @OnMessage
    public void onJsonMessage(String message, Session session) {
        System.out.println(message);

        MessageAction messageAction = (MessageAction) jsonUtil.getProperty(message,
                                                                           "messageAction",
                                                                           MessageAction.class);

        switch (messageAction) {
            case ROUTING_ACTION:
                String deviceId = (String) jsonUtil.getProperty(message, "deviceid", String.class);
                break;
            default:
                LOGGER.error("Unknown message action");
                break;
        }
    }

}
