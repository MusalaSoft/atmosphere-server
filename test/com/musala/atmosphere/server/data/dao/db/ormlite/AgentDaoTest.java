package com.musala.atmosphere.server.data.dao.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.data.model.Agent;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDaoTest {
    private static final String TEST_AGENT_ID = "agent_id";

    private static final String TEST_AGENT_RMI_ID = "rmi_registry_id";

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
        when(mockedAgentDao.create(any(Agent.class))).thenReturn(1);

        testAgentDao.add(TEST_AGENT_ID, TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).create(any(Agent.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveAgentWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);
        when(mockedAgentDao.delete(any(Agent.class))).thenReturn(1);

        testAgentDao.remove(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).delete(any(Agent.class));
    }

    @Test(expected = AgentDaoException.class)
    public void testAddAgentThatAlreadyExists() throws Exception {
        when(mockedAgentDao.create(any(Agent.class))).thenThrow(new SQLException());

        testAgentDao.add(TEST_AGENT_ID, TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).create(any(Agent.class));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AgentDaoException.class)
    public void testRemoveAgentWhenDeleteFailed() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);
        when(mockedAgentDao.delete(any(Agent.class))).thenThrow(new SQLException());

        testAgentDao.remove(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).delete(any(Agent.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveAgentWhichDoesNotExist() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);

        testAgentDao.remove(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));
        verify(mockedAgentDao, times(0)).delete(any(Agent.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAgentIdForExistingAgent() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);

        String receivedAgentId = testAgentDao.getAgentId(EXISTING_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));

        assertEquals("Received agent ID is different from the one that match the query.",
                     EXISTING_AGENT_ID,
                     receivedAgentId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAgentIdForUnexistingAgent() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);

        String receivedAgentId = testAgentDao.getAgentId(TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));
        assertNull("Getting agent ID indicated failure, because agent with the requsted RMI id actually exists.",
                   receivedAgentId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAgentRmiIdForExistingAgent() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);

        String receivedRmiId = testAgentDao.getRmiId(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));

        assertEquals("Received RMI id is different from the one that match the query.",
                     EXISTING_AGENT_RMI_ID,
                     receivedRmiId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAgentRmiIdForUnexistingAgent() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenReturn(resultsList);

        String receivedRmiId = testAgentDao.getRmiId(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));

        assertNull("Getting RMI id indicated failure, because agent with the requsted id actually exists.",
                   receivedRmiId);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AgentDaoException.class)
    public void testGetAgentIdWhenQueryFails() throws Exception {
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenThrow(new SQLException());

        testAgentDao.getAgentId(TEST_AGENT_RMI_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AgentDaoException.class)
    public void testGetAgentRmiIdWhenQueryFails() throws Exception {
        when(mockedAgentDao.queryForFieldValuesArgs(any(Map.class))).thenThrow(new SQLException());

        testAgentDao.getRmiId(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(any(Map.class));
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
