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

package com.musala.atmosphere.server.monitor.ping;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.agent.AgentDisconnectedEvent;
import com.musala.atmosphere.server.websocket.ServerDispatcher;

/**
 * Class that periodically sends ping requests to an agent to determine whether an agent is still connected or must be
 * unregistered.
 *
 * @author filareta.yordanova
 *
 */
public class PingRequestHandler extends Thread {
    private static Logger LOGGER = Logger.getLogger(PingRequestHandler.class.getCanonicalName());

    private static final long PING_INTERVAL = 5000;

    private String agentId;

    private ServerEventService eventService;

    private volatile boolean isRunning;

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    /**
     * Creates a new ping request handler for a specific agent in a separate thread.
     *
     * @param agentId
     *        - the identifier of the agent
     */
    public PingRequestHandler(String agentId) {
        eventService = new ServerEventService();
        isRunning = true;
        this.agentId = agentId;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                dispatcher.sendPong(agentId);
            } catch (IllegalStateException | IOException e) {
                publishEventOnAgentDisconnected();
            }

            try {
                Thread.sleep(PING_INTERVAL);
            } catch (InterruptedException e) {
                String message = String.format("Pinging agent with id %s was interrupted.", agentId);
                LOGGER.warn(message);
            }
        }
    }

    /**
     * Terminates periodically sending ping requests to the agent.
     */
    public synchronized void terminate() {
        isRunning = false;
        String message = String.format("Ping handler for agent %s is terminated.", agentId);
        LOGGER.debug(message);
    }

    private void publishEventOnAgentDisconnected() {
        AgentDisconnectedEvent agentDisconnectedEvent = new AgentDisconnectedEvent(agentId);
        eventService.publish(agentDisconnectedEvent);
    }

}
