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

package com.musala.atmosphere.server.monitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.eventservice.event.agent.AgentConnectedEvent;
import com.musala.atmosphere.server.eventservice.event.agent.AgentDisconnectedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;
import com.musala.atmosphere.server.monitor.ping.PingRequestHandler;

/**
 * Common class responsible for monitoring agents. It listens for all agent related events.
 *
 * @author filareta.yordanova
 *
 */
public class AgentMonitor implements Subscriber {
    private static final Logger LOGGER = Logger.getLogger(AgentMonitor.class);

    private Map<String, PingRequestHandler> pingHandlers = Collections.synchronizedMap(new HashMap<String, PingRequestHandler>());

    /**
     * Informs agent monitor for {@link AgentConnectedEvent event} received when an agent connects.
     *
     * @param event
     *        - event, which is received when an agent is connected.
     */
    public void inform(AgentConnectedEvent event) {
        LOGGER.info("Start pinging...");

        PingRequestHandler pingRequestHandler = new PingRequestHandler(event.getAgentId());
        pingHandlers.put(event.getAgentId(), pingRequestHandler);
        pingRequestHandler.start();
    }

    /**
     * Informs agent monitor for {@link AgentDisconnectedEvent event} received when an agent disconnects.
     *
     * @param event
     *        - event, which is received when an agent is disconnected.
     *
     */
    public void inform(AgentDisconnectedEvent event) {
        PingRequestHandler agentPingHandler = pingHandlers.remove(event.getAgentId());

        if (agentPingHandler != null) {
            agentPingHandler.terminate();
        }
    }

    /**
     * Terminates the ping request sending for all {@link PingRequestHandler ping request handlers}. The action is
     * requested when the connection is closed from the server side.
     *
     * @see com.musala.atmosphere.server.Server#stop()
     *
     */
    public void terminate() {
        for (Map.Entry<String, PingRequestHandler> entry : pingHandlers.entrySet()) {
            entry.getValue().terminate();
        }

        pingHandlers = null;
    }

}
