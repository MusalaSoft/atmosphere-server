package com.musala.atmosphere.server.eventservice.event;

import com.musala.atmosphere.commons.sa.IAgentManager;

/**
 * This event is published when an agent is disconnected, in addition to inform all subscribers that the agent is not
 * available.
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDisconnectedEvent implements AgentEvent {
    private IAgentManager disconnectedAgentManager;

    public AgentDisconnectedEvent(IAgentManager disconnectedAgentManager) {
        this.disconnectedAgentManager = disconnectedAgentManager;
    }

    /**
     * Gets the disconnected agent manager, for which the event was sent.
     * 
     * @return - disconnected agent manager.
     */
    public IAgentManager getDisconnectedAgentManager() {
        return disconnectedAgentManager;
    }

}
