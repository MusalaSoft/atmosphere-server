package com.musala.atmosphere.server.eventservice.event.agent;

import java.rmi.registry.Registry;

import com.musala.atmosphere.commons.sa.IAgentManager;

/**
 * This event is published when an agent is connected to inform all subscribers that requests can be sent to the agent.
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentConnectedEvent implements AgentEvent {
    private IAgentManager connectedAgentManager;

    private Registry agentRegistry;

    /**
     * Creates new event, holding information about the connected agent and it's RMI registry.
     * 
     * @param connectedAgentManager
     *        - agent manager, which is connected.
     * @param agentRegistry
     *        - RMI registry for the connected agent.
     */
    public AgentConnectedEvent(IAgentManager connectedAgentManager, Registry agentRegistry) {
        this.connectedAgentManager = connectedAgentManager;
        this.agentRegistry = agentRegistry;
    }

    /**
     * Gets the connected agent manager, for which the event was sent.
     * 
     * @return connected agent manager.
     */
    public IAgentManager getConnectedAgentManager() {
        return connectedAgentManager;
    }

    /**
     * Gets the RMI registry for the connected agent manager.
     * 
     * @return RMI registry for this agent manager.
     */
    public Registry getAgentRegistry() {
        return agentRegistry;
    }

}
