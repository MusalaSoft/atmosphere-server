package com.musala.atmosphere.server.data.model.ormilite;

import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.musala.atmosphere.server.data.db.constant.AgentColumnName;
import com.musala.atmosphere.server.data.db.constant.TableName;
import com.musala.atmosphere.server.data.model.IAgent;

/**
 * Entity representing an agent, storing all the useful information about it.
 * 
 * @author filareta.yordanova
 * 
 */
@DatabaseTable(tableName = TableName.AGENT)
public class Agent implements IAgent {
    @DatabaseField(columnName = AgentColumnName.ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = AgentColumnName.AGENT_ID, index = true, unique = true, canBeNull = false)
    private String agentId;

    @DatabaseField(columnName = AgentColumnName.HOSTNAME, unique = false, canBeNull = false)
    private String hostname;

    @DatabaseField(columnName = AgentColumnName.PORT, unique = false, canBeNull = false)
    private int port;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<Device> devices;

    public Agent() {
        // all persisted classes must define a no-arg constructor, used when an object is returned from a query
    }

    /**
     * Creates new agent with the given agent ID and RMI id.
     * 
     * @param agentId
     *        - the ID of this agent
     */
    public Agent(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Gets the ID of the agent entry.
     * 
     * @return the ID of the agent entry
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the agent entry.
     * 
     * @param id
     *        - id of the agent entry
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    /**
     * Sets the ID of this agent.
     * 
     * @param agentId
     *        - the ID of this agent
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Gets the hostname of this agent.
     * 
     * @return the hostname of this agent
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname of this agent.
     * 
     * @param hostname
     *        - the hostname of this agent
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets port of this agent.
     * 
     * @return - the port of the agent
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port of this agent.
     * 
     * @param port
     *        - the port of this agent
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets collection of devices for which the agent is responsible.
     * 
     * @return {@link Collection collection} of devices on this agent.
     */
    public Collection<Device> getDevices() {
        return devices;
    }

    /**
     * Sets collection of devices for which the agent is responsible.
     * 
     * @param devices
     *        - collection of devices running on this agent
     */
    public void setDevices(ForeignCollection<Device> devices) {
        this.devices = devices;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(agentId).append(hostname).append(port).toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null || !getClass().equals(object.getClass())) {
            return false;
        }

        Agent agent = (Agent) object;

        return new EqualsBuilder().append(agentId, agent.agentId)
                                  .append(hostname, agent.hostname)
                                  .append(port, agent.port)
                                  .isEquals();
    }
}
