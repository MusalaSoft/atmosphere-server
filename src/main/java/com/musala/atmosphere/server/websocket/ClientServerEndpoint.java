package com.musala.atmosphere.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.cs.util.ClientServerGsonUtil;
import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;
import com.musala.atmosphere.commons.websocket.util.JsonConst;

/**
 * Represents an endpoint of the client-server communication. Handles all Client requests/responses.
 *
 * @author dimcho.nedev
 *
 */
@ServerEndpoint("/client_server")
public class ClientServerEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ClientServerEndpoint.class.getCanonicalName());

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    private static final IJsonUtil jsonUtil = new ClientServerGsonUtil();

    @OnMessage
    public void onJsonMessage(String jsonMessage, Session session) {
        MessageAction messageAction = jsonUtil.getProperty(jsonMessage, JsonConst.MESSAGE_ACTION, MessageAction.class);

        switch (messageAction) {
            case ROUTING_ACTION:
                dispatcher.route(jsonMessage, session);
                break;
            case DEVICE_ALLOCATION_INFORMATION:
                RequestMessage getDeviceAllocationInfoRequest = jsonUtil.deserializeRequest(jsonMessage);
                dispatcher.sendGetDeviceAllocationInfoRequest(getDeviceAllocationInfoRequest, session);
                break;
            case GET_ALL_AVAILABLE_DEVICES:
                RequestMessage getAllDevicesRequest = jsonUtil.deserializeRequest(jsonMessage);
                dispatcher.sendGetAllAvailableDevicesRequest(getAllDevicesRequest, session);
                break;
            case RELEASE_DEVICE:
                RequestMessage releaseRequest = jsonUtil.deserializeRequest(jsonMessage);
                dispatcher.releaseDevice(releaseRequest, session);
                break;
            default:
                LOGGER.error(String.format("Unknown message action on the %s: %s",
                                           this.getClass().getSimpleName(),
                                           messageAction));
                break;
        }
    }

}
