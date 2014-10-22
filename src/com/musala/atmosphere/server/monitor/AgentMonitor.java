package com.musala.atmosphere.server.monitor;

import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IAgentManager;
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
public class AgentMonitor extends Subscriber {
    private static final Logger LOGGER = Logger.getLogger(AgentMonitor.class);

    private Map<IAgentManager, PingRequestHandler> pingHandlers = Collections.synchronizedMap(new HashMap<IAgentManager, PingRequestHandler>());

    /**
     * Informs agent monitor for {@link AgentDisconnectedEvent event} received when an agent disconnects.
     * 
     * @param event
     *        - event, which is received when an agent is disconnected.
     * 
     */
    public void inform(AgentDisconnectedEvent event) {
        IAgentManager disconnectedAgentManager = event.getDisconnectedAgentManager();

        PingRequestHandler agentPingHandler = pingHandlers.remove(disconnectedAgentManager);

        if (agentPingHandler != null) {
            agentPingHandler.terminate();
        }
    }

    /**
     * Informs agent monitor for {@link AgentConnectedEvent event} received when an agent connects.
     * 
     * @param event
     *        - event, which is received when an agent is connected.
     */
    public void inform(AgentConnectedEvent event) {
        IAgentManager connectedAgentManager = event.getConnectedAgentManager();
        Registry agentRegistry = event.getAgentRegistry();

        PingRequestHandler pingRequestHandler = new PingRequestHandler(connectedAgentManager, agentRegistry);
        pingHandlers.put(connectedAgentManager, pingRequestHandler);
        pingRequestHandler.start();
    }

}
