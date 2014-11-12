package com.musala.atmosphere.server.dao.nativeobject;

import java.util.HashMap;

import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.data.model.IAgent;

/**
 * A data access object which handles adding, removing agent, or getting its ID or rmi ID.
 * 
 * @author yavor.stankov
 * 
 */
public class AgentDao implements IAgentDao {
    private HashMap<String, Pair<String, Integer>> agentIdToAgentIpAndPort = new HashMap<String, Pair<String, Integer>>();

    @Override
    public void add(String agentId, String agentIp, int agentPort) {
        Pair agentIpAndPort = new Pair(agentIp, agentPort);

        agentIdToAgentIpAndPort.put(agentId, agentIpAndPort);
    }

    @Override
    public void remove(String agentId) {
        agentIdToAgentIpAndPort.remove(agentId);
    }

    @Override
    public void update(IAgent agent) throws AgentDaoException {
        // Implementation is not needed.
    }

    @Override
    public boolean hasAgent(String agentId) {
        return agentIdToAgentIpAndPort.containsKey(agentId);
    }
}
