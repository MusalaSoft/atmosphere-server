package com.musala.atmosphere.server.dao;

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
     * @param agentIp
     *        - address of the agent
     * @param agentPort
     *        - port that the agent has created it's registry on
     * @throws AgentDaoException
     *         - thrown when adding new agent fails
     */
    public void add(String agentId, String agentIp, int agentPort) throws AgentDaoException;

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
}
