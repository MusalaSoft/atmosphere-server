package com.musala.atmosphere.server.eventservice.event.agent;

/**
 * This event is published when an agent is connected to inform all subscribers that requests can be sent to the agent.
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentConnectedEvent implements AgentEvent {
    private String agentId;

    /**
     * Creates new event, holding information about the connected agent.
     * 
     * @param agentId
     *        - the ID of the agent, which is connected.
     */
    public AgentConnectedEvent(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Gets the identifier of the connected agent
     * 
     * @return agent identifier
     */
    public String getAgentId() {
        return agentId;
    }

}
