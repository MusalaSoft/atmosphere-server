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
     * @param rmiId
     *        - the RMI ID of the entry
     * @throws AgentDaoException
     *         - thrown when adding new agent fails
     */
    public void add(String agentId, String rmiId) throws AgentDaoException;

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
     * Gets the RMI ID of the agent with the given ID.
     * 
     * @param agentId
     *        - ID of the agent
     * @return the RMI ID of the agent with the given ID
     * @throws AgentDaoException
     *         - thrown when retrieving RMI id for the agent from the data source fails or the agent is missing
     */
    // TODO: In future it might be changed to return IAgent by agentId. For now it is included in the interface to make
    // easier the substitution of the
    // implementation using native objects.
    public String getRmiId(String agentId) throws AgentDaoException;

    /**
     * Gets the ID of the agent with the given RMI ID.
     * 
     * @param rmiId
     *        - RMI ID of the agent
     * @return ID of the agent with the given RMI ID
     * @throws AgentDaoException
     *         - thrown when retrieving ID for the agent from the data source fails or the agent is missing
     */
    // TODO: In future it might be changed to return IAgent by rmiId. For now it is included in the interface to make
    // easier the substitution of the
    // implementation using native objects.
    public String getAgentId(String rmiId) throws AgentDaoException;
}
