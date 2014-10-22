package com.musala.atmosphere.server.data.dao.db.ormlite;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Dao<Agent, String> agentDao;

    /**
     * Creates new AgentDao with the given data access object.
     * 
     * @param agentDao
     *        - data access object responsible for operations with agents from the data source
     */
    public AgentDao(Dao<Agent, String> agentDao) throws SQLException {
        this.agentDao = agentDao;
    }

    @Override
    public void add(String agentId, String rmiId) throws AgentDaoException {
        Agent agent = new Agent(agentId, rmiId);

        // TODO: Set hostname and port before agent is created.
        try {
            agentDao.create(agent);
        } catch (SQLException e) {
            String message = String.format("Adding agent with ID %s and RMI registry id %s failed.", agentId, rmiId);
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

    @Override
    public String getRmiId(String agentId) throws AgentDaoException {
        Agent agent = (Agent) selectByAgentId(agentId);

        if (agent != null) {
            return agent.getRmiRegistryId();
        } else {
            String message = String.format("Agent with ID %s does not exist.", agentId);
            throw new AgentDaoException(message);
        }
    }

    @Override
    public String getAgentId(String rmiId) throws AgentDaoException {
        Agent agent = (Agent) selectByRmiId(rmiId);

        if (agent != null) {
            return agent.getAgentId();
        } else {
            String message = String.format("Agent with RMI id %s does not exist.", rmiId);
            throw new AgentDaoException(message);
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

    /**
     * Selects an agent which matches the requested RMI id.
     * 
     * @param rmiId
     *        - the agent RMI id to select by
     * @return {@link IAgent agent} with the requested RMI id or <code>null</code> if such agent is missing
     * @throws AgentDaoException
     *         - thrown when getting an agent from the data source fails
     */
    public IAgent selectByRmiId(String rmiId) throws AgentDaoException {
        try {
            return getAgentByFieldValue(AgentColumnName.RMI_REGISTRY_ID, rmiId);
        } catch (SQLException e) {
            String message = String.format("Selecting agent by RMI id %s failed.", rmiId);
            throw new AgentDaoException(message, e);
        }
    }

    private Agent getAgentByFieldValue(String fieldName, Object fieldValue) throws SQLException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(fieldName, fieldValue);

        List<Agent> resultList = agentDao.queryForFieldValuesArgs(query);

        if (!resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }

}
