package com.musala.atmosphere.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.websocket.message.ClientServerRequest;
import com.musala.atmosphere.commons.websocket.message.ClientServerResponse;
import com.musala.atmosphere.commons.websocket.message.MessageType;

@ServerEndpoint("/server")
public class ClientServerWebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ClientServerWebSocketEndpoint.class.getCanonicalName());

    private ClientServerWebSocketCommunicator websocketCommunicator = ClientServerWebSocketCommunicator.getInstance();

    @OnMessage
    public String onMessage(String message, Session session) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        ClientServerRequest request = gson.fromJson(message, ClientServerRequest.class);

        String sessionId = request.getSessionId();
        MessageType messageType = request.getRequestType();

        ClientServerResponse response = null;
        switch(messageType) {
            case DEVICE_ALLOCATION_INFORMATION:
                DeviceSelector deviceSelector = gson.fromJson(request.getData(), DeviceSelector.class);
                try {
                DeviceAllocationInformation deviceAllocationInformation = websocketCommunicator.getDeviceAllocationInformation(deviceSelector);

                String responseData = gson.toJson(deviceAllocationInformation, DeviceAllocationInformation.class);
                response = new ClientServerResponse(sessionId, messageType, responseData);
                } catch (NoAvailableDeviceFoundException e) {
                    response = new ClientServerResponse(sessionId, messageType, null);
                } catch (NoDeviceMatchingTheGivenSelectorException e) {
                    response = new ClientServerResponse(sessionId, MessageType.ERROR, null);
                }
                break;
            case GET_ALL_DEVICES_REQUEST:
                break;
            case RELEASE_REQUEST:
                break;
            case ROUTING_ACTION:
                break;
            default:
                break;
        }

        String jsonResponse = gson.toJson(response);
        LOGGER.info("Sending string the response back...");
        return jsonResponse;
    }
}
