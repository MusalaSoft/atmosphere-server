package com.musala.atmosphere.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.data.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Keeps track of the connected agents and is responsible for agent allocation.
 *
 * @author yordan.petrov
 *
 */
public class AgentAllocator implements Subscriber {
    private static Logger LOGGER = Logger.getLogger(AgentAllocator.class.getCanonicalName());

    private static IAgentDao agentDao;

    /**
     * Registers that an agent has connected to the server.
     *
     * @param agentId
     *        - an identifier of the agent.
     * @throws AgentDaoException
     *         - thrown when adding agent in the data source fails.
     */
    public void registerAgent(String agentId) throws AgentDaoException {

        if (agentDao.hasAgent(agentId)) {
            String message = String.format("Agent with ID %s is already registered on the server.", agentId);
            LOGGER.warn(message);
            return;
        }

        agentDao.add(agentId);
    }

    /**
     * Unregisters an agent that has been disconnected.
     *
     * @param agentId
     *        - the unique identifier of the agent.
     * @throws AgentDaoException
     *         - when removing the agent from the data source fails
     */
    public void unregisterAgent(String agentId) throws AgentDaoException {
        agentDao.remove(agentId);
    }

    /**
     * Checks whether an agent with a given unique identifier has been registered.
     *
     * @param agentId
     *        - the unique agent identifier
     * @return <code>true</code> if an agent with such identifier is registered, <code>false</code> if such agent is not
     *         registered
     */
    public boolean hasAgent(String agentId) {
        return agentDao.hasAgent(agentId);
    }

    /**
     * Gets a list containing the unique identifiers of all connected agents.
     *
     * @return a list containing the unique identifiers of all connected agents.
     */
    public List<String> getAllConnectedAgentsIds() {
        List<IAgent> agentsList = agentDao.getPresentAgents();
        List<String> connectedAgentsIds = new ArrayList<>();
        for (IAgent agent : agentsList) {
            connectedAgentsIds.add(agent.getAgentId());
        }

        return connectedAgentsIds;
    }

    /**
     * Informs agent allocator for {@link AgentDaoCreatedEvent event} received when an {@link AgentDao data access
     * object} for agents is created.
     *
     * @param event
     *        - event, which is received when data access object for agents is created
     */
    public void inform(AgentDaoCreatedEvent event) {
        IDataSourceProvider dataSourceProvider = new DataSourceProvider();
        agentDao = dataSourceProvider.getAgentDao();
    }
}
