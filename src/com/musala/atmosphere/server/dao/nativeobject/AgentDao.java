package com.musala.atmosphere.server.dao.nativeobject;

import java.util.HashMap;

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
    private HashMap<String, String> agentIdToRmiId = new HashMap<String, String>();

    private HashMap<String, String> rmiIdToAgentId = new HashMap<String, String>();

    @Override
    public void add(String agentId, String rmiId, String agentIp, int agentPort) {
        agentIdToRmiId.put(agentId, rmiId);
        rmiIdToAgentId.put(rmiId, agentId);
    }

    @Override
    public void remove(String agentId) {
        String rmiId = agentIdToRmiId.remove(agentId);
        if (rmiId != null) {
            rmiIdToAgentId.remove(rmiId);
        }
    }

    @Override
    public String getRmiId(String agentId) {
        return agentIdToRmiId.get(agentId);
    }

    @Override
    public String getAgentId(String rmiId) {
        return rmiIdToAgentId.get(rmiId);
    }

    @Override
    public void update(IAgent agent) throws AgentDaoException {
        // Implementation is not needed.
    }
}
