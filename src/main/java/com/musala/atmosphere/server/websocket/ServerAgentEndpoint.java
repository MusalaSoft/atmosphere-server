// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
