package com.musala.atmosphere.server.data.dao.db.ormlite;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.Dao;
import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.data.db.constant.AgentColumnName;
import com.musala.atmosphere.server.data.model.Agent;

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
        Agent agent = new Agent();
        agent.setAgentId(agentId);
        agent.setRmiRegistryId(rmiId);

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
    public String getRmiId(String agentId) throws AgentDaoException {
        Agent agent;

        try {
            agent = getAgentByFieldValue(AgentColumnName.AGENT_ID, agentId);
        } catch (SQLException e) {
            String message = String.format("Getting RMI id for agent with ID %s failed.", agentId);
            throw new AgentDaoException(message, e);
        }

        if (agent != null) {
            return agent.getRmiRegistryId();
        }

        return null;
    }

    @Override
    public String getAgentId(String rmiId) throws AgentDaoException {
        Agent agent;

        try {
            agent = getAgentByFieldValue(AgentColumnName.RMI_REGISTRY_ID, rmiId);
        } catch (SQLException e) {
            String message = String.format("Getting agent id for agent with RMI id %s failed.", rmiId);
            throw new AgentDaoException(message, e);
        }

        if (agent != null) {
            return agent.getAgentId();
        }

        return null;
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
