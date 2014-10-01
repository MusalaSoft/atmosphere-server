package com.musala.atmosphere.server.dao.nativeobject;

import java.util.HashMap;

import com.musala.atmosphere.server.dao.IAgentDAO;

/**
 * A data access object which handles adding, removing agent, or getting its ID or rmi ID.
 * 
 * @author yavor.stankov
 * 
 */
public class AgentDao implements IAgentDAO {
    private HashMap<String, String> agentIdToRmiId = new HashMap<String, String>();

    private HashMap<String, String> rmiIdToAgentId = new HashMap<String, String>();

    @Override
    public boolean add(String agentId, String rmiId) {
        agentIdToRmiId.put(agentId, rmiId);
        rmiIdToAgentId.put(rmiId, agentId);

        return true;
    }

    @Override
    public boolean remove(String agentId) {
        String rmiId = agentIdToRmiId.remove(agentId);
        if (rmiId != null) {
            rmiIdToAgentId.remove(rmiId);
        }

        return true;
    }

    @Override
    public String getRmiId(String agentId) {
        return agentIdToRmiId.get(agentId);
    }

    @Override
    public String getAgentId(String rmiId) {
        return rmiIdToAgentId.get(rmiId);
    }
}
