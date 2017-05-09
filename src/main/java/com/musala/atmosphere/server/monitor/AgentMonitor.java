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

}
