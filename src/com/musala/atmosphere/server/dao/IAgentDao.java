package com.musala.atmosphere.server.dao;

import com.musala.atmosphere.server.dao.exception.AgentDaoException;

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
     *         - thrown when removing agent with the given ID fails
     */
    public void remove(String agentId) throws AgentDaoException;

    /**
     * Gets the RMI ID of the agent with the given Id.
     * 
     * @param agentId
     *        - ID of the agent
     * @return the RMI ID of the agent with the given ID or <code>null</code> if the operation fails
     * @throws AgentDaoException
     *         - thrown when retrieving RMI id for the agent from the data source fails
     */
    public String getRmiId(String agentId) throws AgentDaoException;

    /**
     * Gets the ID of the agent with the given RMI ID.
     * 
     * @param rmiId
     *        - RMI ID of the agent
     * @return ID of the agent with the given RMI ID or <code>null</code> if the operation fails
     * @throws AgentDaoException
     *         - thrown when retrieving ID for the agent from the data source fails
     */
    public String getAgentId(String rmiId) throws AgentDaoException;
}
