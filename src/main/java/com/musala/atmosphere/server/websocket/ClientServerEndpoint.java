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
