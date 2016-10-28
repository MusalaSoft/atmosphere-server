package com.musala.atmosphere.server.eventservice.event.agent;

import com.musala.atmosphere.commons.sa.IAgentManager;

/**
 * This event is published when an agent is disconnected to inform all subscribers that the agent is not available.
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDisconnectedEvent implements AgentEvent {
    private IAgentManager disconnectedAgentManager;

    private String agentId;

    /**
     * Creates new event, holding information about the disconnected agent.
     * 
     * @param disconnectedAgentManager
     *        - agent manager, which is disconnected
     * @param agentId
     *        - the ID of the disconnected agent manager
     */
    public AgentDisconnectedEvent(IAgentManager disconnectedAgentManager, String agentId) {
        this.disconnectedAgentManager = disconnectedAgentManager;
        this.agentId = agentId;
    }

    /**
     * Gets the disconnected agent manager, for which the event was sent.
     * 
     * @return - disconnected agent manager
     */
    public IAgentManager getDisconnectedAgentManager() {
        return disconnectedAgentManager;
    }

    /**
     * Gets the ID of the disconnected agent manager.
     * 
     * @return - the agent ID
     */
    public String getAgentId() {
        return agentId;
    }
}
