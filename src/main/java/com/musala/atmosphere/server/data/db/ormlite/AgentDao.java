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

package com.musala.atmosphere.server.data.db.ormlite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.j256.ormlite.dao.Dao;
import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.AgentDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.AgentColumnName;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.model.ormilite.Agent;

/**
 * Class that provides a wrapper for ORMLite data access object manager, used for executing operations with agents from
 * the data source.
 *
 * @author filareta.yordanova
 *
 */
public class AgentDao implements IAgentDao {
    private static final Logger LOGGER = Logger.getLogger(AgentDao.class);

    private Dao<Agent, String> agentDao;

    /**
     * Creates new AgentDao with the given data access object.
     *
     * @param agentDao
     *        - data access object responsible for operations with agents from the data source
     * @throws SQLException
     *         An exception that provides information on a database access error or other errors
     */
    public AgentDao(Dao<Agent, String> agentDao) throws SQLException {
        this.agentDao = agentDao;
    }

    @Override
    public void add(String agentId) throws AgentDaoException {
        Agent agent = new Agent(agentId);

        try {
            agentDao.create(agent);
        } catch (SQLException e) {
            String message = String.format("Adding agent with ID %s failed", agentId);
            throw new AgentDaoException(message, e);
        }
    }

    @Override
    public void remove(String agentId) throws AgentDaoException {
        try {
            Agent agentToDelete = getAgentByFieldValue(AgentColumnName.AGENT_ID, agentId);

            if (agentToDelete == null) {
                return;
            }

            agentDao.delete(agentToDelete);
        } catch (SQLException e) {
            String message = String.format("Removing agent with ID %s failed.", agentId);
            throw new AgentDaoException(message, e);
        }
    }

    @Override
    public void update(IAgent agent) throws AgentDaoException {
        if (agent == null) {
            // No need to invoke update, it will return 0 but the update is not actually executed.
            throw new AgentDaoRuntimeException("The agent you are trying to update is null.");
        }

        try {
            agentDao.update((Agent) agent);
        } catch (SQLException e) {
            String agentId = ((Agent) agent).getAgentId();
            String message = String.format("Updating agent with ID %s failed, because data source failed.", agentId);
            throw new AgentDaoException(message, e);
        }
    }

    /**
     * Selects an agent which matches the requested ID.
     *
     * @param agentId
     *        - the agent ID to select by
     * @return {@link IAgent agent} with the requested ID or <code>null</code> if such agent is missing
     * @throws AgentDaoException
     *         - thrown when getting an agent from the data source fails
     */
    public IAgent selectByAgentId(String agentId) throws AgentDaoException {
        try {
            return getAgentByFieldValue(AgentColumnName.AGENT_ID, agentId);
        } catch (SQLException e) {
            String message = String.format("Selecting agent by ID %s failed.", agentId);
            throw new AgentDaoException(message, e);
        }
    }

    private Agent getAgentByFieldValue(String fieldName, Object fieldValue) throws SQLException {
        Map<String, Object> query = new HashMap<>();
        query.put(fieldName, fieldValue);

        List<Agent> resultList = agentDao.queryForFieldValuesArgs(query);

        if (!resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }

    @Override
    public boolean hasAgent(String agentId) {
        try {
            return getAgentByFieldValue(AgentColumnName.AGENT_ID, agentId) != null;
        } catch (SQLException e) {
            String message = String.format("Getting agent with ID %s from the data source fails.", agentId);
            LOGGER.error(message, e);
            return false;
        }
    }

    @Override
    public List<IAgent> getPresentAgents() {
        try {
            List<Agent> agentsFromDataSource = agentDao.queryForAll();
            return new ArrayList<IAgent>(agentsFromDataSource);
        } catch (SQLException e) {
            return new ArrayList<IAgent>();
        }
    }
}
