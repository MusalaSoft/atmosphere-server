package com.musala.atmosphere.server.eventservice.event.agent;

/**
 * This event is published when an agent is disconnected to inform all subscribers that the agent is not available.
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDisconnectedEvent implements AgentEvent {
    private String agentId;

    /**
     * Creates new event, holding information about the disconnected agent.
     * 
     * @param agentId
     *        - the ID of the disconnected agent
     */
    public AgentDisconnectedEvent(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Gets the ID of the disconnected agent.
     * 
     * @return - the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

}
