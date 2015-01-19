package com.musala.atmosphere.server;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
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

    private static Map<String, Registry> agentIdToRegistry = Collections.synchronizedMap(new HashMap<String, Registry>());

    private static IAgentDao agentDao;

    /**
     * Registers that an agent has connected to the server.
     * 
     * @param agentManager
     *        - the {@link IAgentManager} of the connected agent.
     * @param agentRegistry
     *        - the RMI {@link Registry} of the connected agent.
     * @throws RemoteException
     *         - thrown when connection to agent is lost
     * @throws AgentDaoException
     *         - thrown when adding agent in the data source fails
     */
    public void registerAgent(IAgentManager agentManager, Registry agentRegistry, String agentIp, int agentPort)
        throws RemoteException,
            AgentDaoException {
        String agentId = agentManager.getAgentId();

        if (agentDao.hasAgent(agentId)) {
            String message = String.format("Agent with ID %s is already registered on the server.", agentId);
            LOGGER.warn(message);
            return;
        }

        agentDao.add(agentId, agentIp, agentPort);
        agentIdToRegistry.put(agentId, agentRegistry);
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
     * Gets the RMI {@link Registry registry} of connected agent by a given unique agent identifier.
     * 
     * @param agentId
     *        - the unique agent identifier
     * @return the RMI {@link Registry registry} of connected agent by a given unique agent identifier
     */
    public Registry getAgentRegistry(String agentId) {
        return agentIdToRegistry.get(agentId);
    }

    /**
     * Gets the {@link IAgentManager manager} of connected agent by a given unique agent identifier.
     * 
     * @param agentId
     *        - the unique agent identifier.
     * @return the {@link IAgentManager manager}of connected agent by a given unique agent identifier.
     * @throws AccessException
     *         - thrown if finding {@link IAgentManager manager} from the RMI {@link Registry agent registry} fails
     * @throws RemoteException
     *         - thrown if connection to agent is lost
     * @throws NotBoundException
     *         - thrown if finding {@link IAgentManager manager} from the RMI {@link Registry agent registry} fails
     */
    public IAgentManager getAgentManager(String agentId) throws AccessException, RemoteException, NotBoundException {
        Registry agentRegistry = agentIdToRegistry.get(agentId);
        return (IAgentManager) agentRegistry.lookup(RmiStringConstants.AGENT_MANAGER.toString());
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
        List<String> connectedAgentsIds = new ArrayList<String>();
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
