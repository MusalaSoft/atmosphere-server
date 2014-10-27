package com.musala.atmosphere.server.data.dao.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.AgentDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.AgentColumnName;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.model.ormilite.Agent;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDaoTest {
    private static final String TEST_AGENT_ID = "agent_id";

    private static final String TEST_AGENT_RMI_ID = "rmi_registry_id";

    private static final String TEST_AGENT_IP = "agent_ip";

    private static final int TEST_AGENT_PORT = 1234;

    private static final String EXISTING_AGENT_ID = "existing_agent_id";

    private static final String EXISTING_AGENT_RMI_ID = "existing_rmi_registry_id";

    private static AgentDao testAgentDao;

    private static Dao<Agent, String> mockedAgentDao;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        mockedAgentDao = mock(Dao.class);
        testAgentDao = new AgentDao(mockedAgentDao);
    }

    @Test
    public void testAddNewAgent() throws Exception {
        Agent expectedAgentToCreate = new Agent(TEST_AGENT_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.create(eq(expectedAgentToCreate))).thenReturn(1);

        testAgentDao.add(TEST_AGENT_ID, TEST_AGENT_RMI_ID, TEST_AGENT_IP, TEST_AGENT_PORT);
        verify(mockedAgentDao, times(1)).create(eq(expectedAgentToCreate));
    }

    @Test
    public void testRemoveAgentWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Agent expectedAgentToDelete = new Agent(EXISTING_AGENT_ID, EXISTING_AGENT_RMI_ID);
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, EXISTING_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);
        when(mockedAgentDao.delete(eq(expectedAgentToDelete))).thenReturn(1);

        testAgentDao.remove(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
        verify(mockedAgentDao, times(1)).delete(eq(expectedAgentToDelete));
    }

    @Test(expected = AgentDaoException.class)
    public void testAddAgentThatAlreadyExists() throws Exception {
        Agent expectedAgentToCreate = new Agent(TEST_AGENT_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.create(expectedAgentToCreate)).thenThrow(new SQLException());

        testAgentDao.add(TEST_AGENT_ID, TEST_AGENT_RMI_ID, TEST_AGENT_IP, TEST_AGENT_PORT);
        verify(mockedAgentDao, times(1)).create(eq(expectedAgentToCreate));
    }

    @Test
    public void testUpdateExistingAgent() throws Exception {
        Agent agentToUpdate = new Agent(TEST_AGENT_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.update(eq(agentToUpdate))).thenReturn(1);

        testAgentDao.update(agentToUpdate);
        verify(mockedAgentDao, times(1)).update(eq(agentToUpdate));
    }

    @Test(expected = AgentDaoException.class)
    public void testUpdateAgentWhenUpdateFailed() throws Exception {
        Agent agentToUpdate = new Agent(TEST_AGENT_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.update(eq(agentToUpdate))).thenThrow(new SQLException());

        testAgentDao.update(agentToUpdate);
        verify(mockedAgentDao, times(1)).update(eq(agentToUpdate));
    }

    @Test(expected = AgentDaoRuntimeException.class)
    public void testUpdateAgentWhenAgentIsNull() throws Exception {
        Agent agentToUpdate = null;

        testAgentDao.update(agentToUpdate);
        verify(mockedAgentDao, times(0)).update(eq(agentToUpdate));
    }

    @Test(expected = AgentDaoException.class)
    public void testRemoveAgentWhenDeleteFailed() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, EXISTING_AGENT_ID);
        Agent expectedAgentToDelete = new Agent(EXISTING_AGENT_ID, EXISTING_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);
        when(mockedAgentDao.delete(eq(expectedAgentToDelete))).thenThrow(new SQLException());

        testAgentDao.remove(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
        verify(mockedAgentDao, times(1)).delete(eq(expectedAgentToDelete));
    }

    @Test
    public void testRemoveAgentWhichDoesNotExist() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, TEST_AGENT_ID);
        Agent expectedAgentToDelete = new Agent(TEST_AGENT_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        testAgentDao.remove(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
        verify(mockedAgentDao, times(0)).delete(eq(expectedAgentToDelete));
    }

    @Test
    public void testGetAgentIdForExistingAgent() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.RMI_REGISTRY_ID, EXISTING_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        String receivedAgentId = testAgentDao.getAgentId(EXISTING_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertEquals("Received agent ID is different from the one that match the query.",
                     EXISTING_AGENT_ID,
                     receivedAgentId);
    }

    @Test(expected = AgentDaoException.class)
    public void testGetAgentIdForUnexistingAgent() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.RMI_REGISTRY_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        testAgentDao.getAgentId(TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
    }

    @Test
    public void testGetAgentRmiIdForExistingAgent() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, EXISTING_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        String receivedRmiId = testAgentDao.getRmiId(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertEquals("Received RMI id is different from the one that match the query.",
                     EXISTING_AGENT_RMI_ID,
                     receivedRmiId);
    }

    @Test(expected = AgentDaoException.class)
    public void testGetAgentRmiIdForUnexistingAgent() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, TEST_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        testAgentDao.getRmiId(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
    }

    @Test(expected = AgentDaoException.class)
    public void testGetAgentIdWhenQueryFails() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.RMI_REGISTRY_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenThrow(new SQLException());

        testAgentDao.getAgentId(TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
    }

    @Test(expected = AgentDaoException.class)
    public void testGetAgentRmiIdWhenQueryFails() throws Exception {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, TEST_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenThrow(new SQLException());

        testAgentDao.getRmiId(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
    }

    @Test
    public void testSelectAgentByIdWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Agent expectedAgent = new Agent(EXISTING_AGENT_ID, EXISTING_AGENT_RMI_ID);
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, EXISTING_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        IAgent receivedAgent = testAgentDao.selectByAgentId(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertEquals("The agent received with the requested ID does not match the expected one.",
                     expectedAgent,
                     receivedAgent);
    }

    @Test
    public void testSelectAgentByRmiIdWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Agent expectedAgent = new Agent(EXISTING_AGENT_ID, EXISTING_AGENT_RMI_ID);
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.RMI_REGISTRY_ID, EXISTING_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        IAgent receivedAgent = testAgentDao.selectByRmiId(EXISTING_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertEquals("The agent received with the requested RMI id does not match the expected one.",
                     expectedAgent,
                     receivedAgent);
    }

    @Test
    public void testSelectAgentByIdWhenAgentDoesNotExist() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, TEST_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        IAgent receivedAgent = testAgentDao.selectByAgentId(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertNull("Found an agent matching the requested ID, even though it does not exist.", receivedAgent);
    }

    @Test
    public void testSelectAgentByRmiIdWhenAgentDoesNotExist() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.RMI_REGISTRY_ID, TEST_AGENT_RMI_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        IAgent receivedAgent = testAgentDao.selectByRmiId(TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertNull("Found an agent matching the requested RMI id, even though it does not exist.", receivedAgent);
    }

    private List<Agent> getFakeResultList() {
        List<Agent> resultsList = new ArrayList<Agent>();
        Agent existingAgent = new Agent();
        existingAgent.setAgentId(EXISTING_AGENT_ID);
        existingAgent.setRmiRegistryId(EXISTING_AGENT_RMI_ID);
        resultsList.add(existingAgent);
        return resultsList;
    }
}
