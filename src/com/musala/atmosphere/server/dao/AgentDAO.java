package com.musala.atmosphere.server.dao;

/**
 * A data access object for using agent information from a data source.
 * 
 * @author delyan.dimitrov
 * 
 */
public interface AgentDAO {
    /**
     * Adds an agent entry in the data source.
     * 
     * @param agentId
     *        - the ID of the entry
     * @param rmiId
     *        - the RMI ID of the entry
     * @return <code>true</code> if the add operation is successful, and <code>false</code> false otherwise
     */
    public boolean add(String agentId, String rmiId);

    /**
     * Removes the entry of the agent with the given ID from the data source.
     * 
     * @param agentId
     *        - ID of the agent to be removed
     * @return <code>true</code> if the removal is successful, and <code>false</code> false otherwise
     */
    public boolean remove(String agentId);

    /**
     * Gets the RMI ID of the agent with the given Id.
     * 
     * @param agentId
     *        - ID of the agent
     * @return the RMI ID of the agent with the given ID or <code>null</code> if the operation fails
     */
    public String getRmiId(String agentId);

    /**
     * Gets the ID of the agent with the given RMI ID.
     * 
     * @param rmiId
     *        - RMI ID of the agent
     * @return ID of the agent with the given RMI ID or <code>null</code> if the operation fails
     */
    public String getAgentId(String rmiId);
}
