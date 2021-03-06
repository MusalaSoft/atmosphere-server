// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.server.data.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import com.musala.atmosphere.server.data.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.model.ormilite.Agent;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDaoTest {
    private static final String TEST_AGENT_ID = "agent_id";

    private static final String EXISTING_AGENT_ID = "existing_agent_id";

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
        Agent expectedAgentToCreate = new Agent(TEST_AGENT_ID);

        when(mockedAgentDao.create(eq(expectedAgentToCreate))).thenReturn(1);

        testAgentDao.add(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).create(eq(expectedAgentToCreate));
    }

    @Test
    public void testRemoveAgentWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Agent expectedAgentToDelete = new Agent(EXISTING_AGENT_ID);
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
        Agent expectedAgentToCreate = new Agent(TEST_AGENT_ID);

        when(mockedAgentDao.create(expectedAgentToCreate)).thenThrow(new SQLException());

        testAgentDao.add(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).create(eq(expectedAgentToCreate));
    }

    @Test
    public void testUpdateExistingAgent() throws Exception {
        Agent agentToUpdate = new Agent(TEST_AGENT_ID);

        when(mockedAgentDao.update(eq(agentToUpdate))).thenReturn(1);

        testAgentDao.update(agentToUpdate);
        verify(mockedAgentDao, times(1)).update(eq(agentToUpdate));
    }

    @Test(expected = AgentDaoException.class)
    public void testUpdateAgentWhenUpdateFailed() throws Exception {
        Agent agentToUpdate = new Agent(TEST_AGENT_ID);

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
        Agent expectedAgentToDelete = new Agent(EXISTING_AGENT_ID);

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
        Agent expectedAgentToDelete = new Agent(TEST_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        testAgentDao.remove(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));
        verify(mockedAgentDao, times(0)).delete(eq(expectedAgentToDelete));
    }

    @Test
    public void testSelectAgentByIdWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Agent expectedAgent = new Agent(EXISTING_AGENT_ID);
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
    public void testHasAgentWithIdWhenAgentExists() throws Exception {
        List<Agent> resultsList = getFakeResultList();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, EXISTING_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        boolean hasAgent = testAgentDao.hasAgent(EXISTING_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertTrue("Result is different from the expected, agent with the requested ID was not found.", hasAgent);
    }

    @Test
    public void testHasAgentWithIdWhenAgentDoesNotExist() throws Exception {
        List<Agent> resultsList = new ArrayList<Agent>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, TEST_AGENT_ID);

        when(mockedAgentDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        boolean hasAgent = testAgentDao.hasAgent(TEST_AGENT_ID);
        verify(mockedAgentDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertFalse("Returned result is different from the expected, agent with the given ID actualy exists.", hasAgent);
    }

    @Test
    public void testGetAgentsWhenThereAreNoAgents() throws Exception {
        List<IAgent> receivedAgentsList = testAgentDao.getPresentAgents();
        assertTrue("The list is not empty when no agents were added.", receivedAgentsList.isEmpty());
    }

    @Test
    public void testGetAgentsWhenThereAreExistingAgents() throws Exception {
        Agent expectedAgentToCreate = new Agent(TEST_AGENT_ID);
        List<Agent> expectedAgentsList = new ArrayList<Agent>();
        expectedAgentsList.add(expectedAgentToCreate);

        when(mockedAgentDao.create(eq(expectedAgentToCreate))).thenReturn(1);
        when(mockedAgentDao.queryForAll()).thenReturn(expectedAgentsList);

        testAgentDao.add(TEST_AGENT_ID);
        final int EXPECTED_AGENTS_COUNT = 1;
        int receivedAgentsCount = testAgentDao.getPresentAgents().size();
        IAgent receivedAgent = testAgentDao.getPresentAgents().get(0);
        assertEquals("Received agents count is different than expected.", receivedAgentsCount, EXPECTED_AGENTS_COUNT);
        assertEquals("The rceived agent is different from the added one.", receivedAgent, expectedAgentToCreate);
    }

    private List<Agent> getFakeResultList() {
        List<Agent> resultsList = new ArrayList<Agent>();
        Agent existingAgent = new Agent();
        existingAgent.setAgentId(EXISTING_AGENT_ID);
        resultsList.add(existingAgent);
        return resultsList;
    }
}
