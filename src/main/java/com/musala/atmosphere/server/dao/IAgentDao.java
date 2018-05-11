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

package com.musala.atmosphere.server.dao;

import java.util.List;

import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.data.model.IAgent;

/**
 * A data access object for using agent information from a data source.
 *
 * @author delyan.dimitrov
 *
 */
public interface IAgentDao {
    /**
     * Adds an agent entry in the data source.
     *
     * @param agentId
     *        - the ID of the entry
     * @throws AgentDaoException
     *         - thrown when adding new agent fails
     */
    public void add(String agentId) throws AgentDaoException;

    /**
     * Removes the entry of the agent with the given ID from the data source.
     *
     * @param agentId
     *        - ID of the agent to be removed
     * @throws AgentDaoException
     *         - thrown when removing agent fails
     */
    public void remove(String agentId) throws AgentDaoException;

    /**
     * Updates the given agent in the data source.
     *
     * @param agent
     *        - agent whose information will be updated in the data source
     * @throws AgentDaoException
     *         - thrown when updating agent fails
     *
     */
    public void update(IAgent agent) throws AgentDaoException;

    /**
     * Checks if agent with the given ID exists in the data source.
     *
     * @param agentId
     *        - unique identifier used for search criterion
     * @return <code>true</code> if agent with this ID exists and <code>false</code> otherwise
     */
    public boolean hasAgent(String agentId);

    /**
     * Used to get all agents that are registered in the data source.
     *
     * @return List containing all the agents in the data source.
     */
    public List<IAgent> getPresentAgents();
}
