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
