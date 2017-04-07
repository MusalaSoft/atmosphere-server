package com.musala.atmosphere.server.websocket;

import java.lang.reflect.Type;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.DeviceNotFoundException;
import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.commons.util.structure.tree.Tree;
import com.musala.atmosphere.commons.websocket.message.ClientServerRequest;
import com.musala.atmosphere.commons.websocket.message.ClientServerResponse;
import com.musala.atmosphere.commons.websocket.message.MessageType;

@ServerEndpoint("/server")
public class ClientServerWebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ClientServerWebSocketEndpoint.class.getCanonicalName());

    private ClientServerWebSocketCommunicator websocketCommunicator = ClientServerWebSocketCommunicator.getInstance();

    private RoutingAction routingAction;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @OnMessage
    public String onMessage(String message, Session session) {
        ClientServerRequest request = gson.fromJson(message, ClientServerRequest.class);

        String sessionId = request.getSessionId();
        MessageType messageType = request.getRequestType();

        ClientServerResponse response = null;
        switch(messageType) {
            case DEVICE_ALLOCATION_INFORMATION:
                LOGGER.info("Received device allocation request.");
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
                LOGGER.info("Received request to get all available devices.");
                try {
                    List<Pair<String, String>> availableDevicesList = websocketCommunicator.getAllAvailableDevices();
                    TypeToken<List<Pair<String, String>>> listTypeToken = new TypeToken<List<Pair<String, String>>>() {};
                    String responseData = gson.toJson(availableDevicesList, listTypeToken.getType());
                    response = new ClientServerResponse(sessionId, messageType, responseData);
                } catch (RemoteException e) {
                    response = new ClientServerResponse(sessionId, MessageType.ERROR, null);
                }
                break;
            case RELEASE_REQUEST:
                LOGGER.info("Received device release request.");
                DeviceAllocationInformation deviceDescriptor = gson.fromJson(request.getData(), DeviceAllocationInformation.class);
                websocketCommunicator.releaseDevice(deviceDescriptor);
                response = new ClientServerResponse(sessionId, messageType, null);
                break;
            case ROUTING_ACTION:
                LOGGER.info("Received device routing action request.");
                try {
                    Object result = sendRoutingAction(request.getData());
                    if (result == null) {
                        // The action ran successfully on the agent, but it did not return a result
                        response = new ClientServerResponse(sessionId, messageType, null);
                        LOGGER.info("Response is null.");
                    } else {
                        LOGGER.info("Response class: " + result.getClass().getName());
                        // Put the action result and its class in the response, so we can deserialize it
                        // correctly on the other end when type erasure occurs. This should probably be
                        // done on the agent when we fully migrate the communication.
                        // serializeCorrectlyIfCollection is a workaround for the cases when the response is a collection.
                        // Generic types do not play well with Gson serialization and deserialization:
                        // https://sites.google.com/site/gson/gson-user-guide#TOC-Serializing-and-Deserializing-Generic-Types
                        JsonObject jsonResultWithClass = new JsonObject();
                        jsonResultWithClass.addProperty("class", result.getClass().getName());
                        jsonResultWithClass.addProperty("value", gson.toJson(result, serializeCorrectlyIfCollection(result)));
                        LOGGER.info(jsonResultWithClass.toString());
                        response = new ClientServerResponse(sessionId, messageType, jsonResultWithClass.toString());
                    }
                } catch (RemoteException | NotBoundException e) { // this block should be gone when we fully migrate to WebSockets
                    response = new ClientServerResponse(sessionId, MessageType.ERROR, e.getMessage());
                } catch (CommandFailedException | InvalidPasskeyException | DeviceNotFoundException | ClassNotFoundException e) {
                    response = new ClientServerResponse(sessionId, MessageType.ERROR, e.getMessage());
                }
                break;
            default:
                LOGGER.error("The request was of unexpected type.");
                break;
        }

        return gson.toJson(response);
    }

    private Object sendRoutingAction(String requestData)
            throws AccessException, RemoteException, NotBoundException,
            CommandFailedException, InvalidPasskeyException, DeviceNotFoundException, ClassNotFoundException {
        JsonObject json = new JsonParser().parse(requestData).getAsJsonObject();

        String deviceRmiId = json.get("rmiId").getAsString();
        long passkey = json.get("passkey").getAsLong();
        RoutingAction action = gson.fromJson(json.get("action").getAsString(), RoutingAction.class);
        this.routingAction = action; // Used with the workaround
        LOGGER.info("Routing action:" + action);

        List<Object> argsList = new ArrayList<Object>();
        JsonArray argsArray = json.get("args").getAsJsonArray();
        for (JsonElement jsonArg : argsArray) {
            JsonObject jsonObject = jsonArg.getAsJsonObject();
            Class<?> objectClass = Class.forName(jsonObject.get("class").getAsString());
            Object argObject = gson.fromJson(jsonObject.get("value").getAsString(), objectClass);
            argsList.add(argObject);
            LOGGER.info("Adding argument of type " + argObject.getClass().getName() + ": " + argObject.toString());
        }

        Object[] argsObjectArray = argsList.toArray();

        return websocketCommunicator.routeAction(deviceRmiId, passkey, action, argsObjectArray);
    }

    private Type serializeCorrectlyIfCollection(Object object) {
        Type objectType = null;
        if (routingAction == RoutingAction.EXECUTE_SHELL_COMMAND_SEQUENCE) {
            objectType = new TypeToken<List<String>>(){}.getType();
        } else if (routingAction == RoutingAction.GET_UI_TREE) {
            objectType = new TypeToken<Tree<AccessibilityElement>>(){}.getType();
        } else if (routingAction == RoutingAction.GET_UI_ELEMENTS ||
                routingAction == RoutingAction.GET_CHILDREN ||
                routingAction == RoutingAction.EXECUTE_XPATH_QUERY_ON_LOCAL_ROOT ||
                routingAction == RoutingAction.EXECUTE_XPATH_QUERY) {
            objectType = new TypeToken<List<AccessibilityElement>>(){}.getType();
        } else if (routingAction == RoutingAction.GET_LOGCAT_BUFFER) {
            objectType = new TypeToken<List<Pair<Integer, String>>>(){}.getType();
        } else if (routingAction == RoutingAction.GET_WEB_VIEWS) {
            objectType = new TypeToken<Set<String>>(){}.getType();
        } else if (routingAction == RoutingAction.FIND_WEB_ELEMENT) {
            objectType = new TypeToken<Map<String, Object>>(){}.getType();
        } else if (routingAction == RoutingAction.FIND_WEB_ELEMENTS) {
            objectType = new TypeToken<List<Map<String, Object>>>(){}.getType();
        } else {
            return object.getClass();
        }
        return objectType;
    }
}
