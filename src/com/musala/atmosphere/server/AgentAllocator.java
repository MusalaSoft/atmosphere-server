package com.musala.atmosphere.server;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.util.Pair;

/**
 * Keeps track of the connected agents and is responsible for agent allocation.
 * 
 * @author yordan.petrov
 * 
 */
public class AgentAllocator {
    private static Logger LOGGER = Logger.getLogger(AgentAllocator.class.getCanonicalName());

    private static Map<String, Pair<IAgentManager, Registry>> agentManagersId = Collections.synchronizedMap(new HashMap<String, Pair<IAgentManager, Registry>>());

    /**
     * Registers that an agent has connected to the server.
     * 
     * @param agentManager
     *        - the {@link IAgentManager} of the connected agent.
     * @param agentRegistry
     *        - the RMI {@link Registry} of the connected agent.
     * @throws RemoteException
     */
    public void registerAgent(IAgentManager agentManager, Registry agentRegistry) throws RemoteException {
        String agentId = agentManager.getAgentId();
        Pair<IAgentManager, Registry> foundAgentRegistryPair = agentManagersId.get(agentId);
        if (foundAgentRegistryPair != null) {
            LOGGER.warn("Agent with id " + agentId + " is already registered on the server.");
            return;
        }

        Pair<IAgentManager, Registry> connectedAgentRegistryPair = new Pair<IAgentManager, Registry>(agentManager,
                                                                                                     agentRegistry);
        agentManagersId.put(agentId, connectedAgentRegistryPair);
    }

    /**
     * Unregisters an agent that has been disconnected.
     * 
     * @param agentId
     *        - the unique indetifier of the agent.
     */
    public void unregisterAgent(String agentId) {
        Pair<IAgentManager, Registry> foundAgentRegistryPair = agentManagersId.remove(agentId);

        if (foundAgentRegistryPair == null) {
            LOGGER.warn("Agent with id " + agentId + " is not registered on the server.");
        }
    }

    /**
     * Gets an {@link IAgentManager} instance of the agent with highest performance score.
     * 
     * @return an {@link IAgentManager} instance of the agent with highest performance score.
     */
    public IAgentManager getAgent() {
        // TODO: Add load balancing logic
        for (Entry<String, Pair<IAgentManager, Registry>> agentEntry : agentManagersId.entrySet()) {
            return agentEntry.getValue().getKey();
        }

        return null;
    }

    /**
     * Gets an {@link IAgentManager}, RMI {@link Registry} {@link Pair} of connected agent by a given unique agent
     * identifier.
     * 
     * @param agentId
     *        - the unique agent identifier.
     * @return an {@link IAgentManager}, RMI {@link Registry} {@link Pair} of connected agent by a given unique agent
     *         identifier.
     */
    public Pair<IAgentManager, Registry> getAgentRegistryPair(String agentId) {
        return agentManagersId.get(agentId);
    }

    /**
     * Gets an {@link IAgentManager} of connected agent by a given unique agent identifier.
     * 
     * @param agentId
     *        - the unique agent identifier.
     * @return an {@link IAgentManager} of connected agent by a given unique agent identifier.
     */
    public IAgentManager getAgentById(String agentId) {
        return agentManagersId.get(agentId).getKey();
    }

    /**
     * Checks whether an agent with a given unique identifier has been registered.
     * 
     * @param agentId
     *        - the unique agent identifier.
     * @return <code>true</code> if an agent with such identifier is registered, <code>false</code> if such agent is not
     *         registered.
     */
    public boolean hasAgent(String agentId) {
        return agentManagersId.containsKey(agentId);
    }

    /**
     * Gets a list containing the {@link IAgentManager}s of all connected agents.
     * 
     * @return a list containing the {@link IAgentManager}s of all connected agents.
     */
    public List<IAgentManager> getAllConnectedAgents() {
        List<IAgentManager> connectedAgentManagerList = new ArrayList<IAgentManager>();
        synchronized (agentManagersId) {
            for (Pair<IAgentManager, Registry> agentRegistryPair : agentManagersId.values()) {
                connectedAgentManagerList.add(agentRegistryPair.getKey());
            }
        }
        return connectedAgentManagerList;
    }

    /**
     * Gets a list containing the unique identifiers of all connected agents.
     * 
     * @return a list containing the unique identifiers of all connected agents.
     */
    public List<String> getAllConnectedAgentsIds() {
        Collection<String> connectedAgentManagersIdSet = agentManagersId.keySet();
        List<String> connectedAgentManagersIdList = new ArrayList<String>();
        connectedAgentManagersIdList.addAll(connectedAgentManagersIdSet);
        return connectedAgentManagersIdList;
    }
}
