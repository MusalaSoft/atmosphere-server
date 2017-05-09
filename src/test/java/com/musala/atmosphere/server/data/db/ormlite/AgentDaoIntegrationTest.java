package com.musala.atmosphere.server.data.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.AgentDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.AgentColumnName;
import com.musala.atmosphere.server.data.db.constant.Property;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.model.ormilite.Agent;

/**
 *
 * @author denis.bialev
 *
 */
public class AgentDaoIntegrationTest {

    private static final String TEST_AGENT_ID = "agent_id";

    private static final String UPDATED_TEST_AGENT_ID = "updated_agent_id";

    private static final String NON_EXISTING_AGENT_ID = "non_existing_agent_id";

    private static final String SECOND_TEST_AGENT_ID = "second_agent_id";

    private static AgentDao testAgentDao;

    private static AgentDaoTestUtils agentDaoTestUtils;

    private static Agent testAgent;

    private static Agent secondTestAgent;

    private static Dao<Agent, String> ormliteAgentDao;

    private static final int EXPECTED_AGENTS_COUNT = 2;

    @BeforeClass
    public static void setUpTest() throws Exception {
        agentDaoTestUtils = new AgentDaoTestUtils();
        agentDaoTestUtils.setUpDatabase();

        testAgentDao = agentDaoTestUtils.getAgentDao();

        assertNotNull("Initialization of the database failed. ", testAgentDao);

        JdbcConnectionSource connectionSource = new JdbcConnectionSource(Property.DATABASE_URL);
        ormliteAgentDao = DaoManager.createDao(connectionSource, Agent.class);

        testAgent = new Agent(TEST_AGENT_ID);

        secondTestAgent = new Agent(SECOND_TEST_AGENT_ID);
    }

    private void deleteAgent(String agentId) throws SQLException, AgentDaoException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(AgentColumnName.AGENT_ID, agentId);

        List<Agent> resultList = ormliteAgentDao.queryForFieldValuesArgs(query);

        if (!resultList.isEmpty()) {
            ormliteAgentDao.delete(resultList.get(0));
        }
    }

    @After
    public void afterTest() throws Exception {
        deleteAgent(TEST_AGENT_ID);
        deleteAgent(SECOND_TEST_AGENT_ID);
        deleteAgent(UPDATED_TEST_AGENT_ID);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        agentDaoTestUtils.unsubscribe();
    }

    @Test
    public void testAddNewAgent() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);
    }

    @Test
    public void testRemoveAgentWhenAgentExists() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);
        agentDaoTestUtils.assertRemoveAgent(testAgent);
    }

    @Test(expected = AgentDaoException.class)
    public void testAddAgentThatAlreadyExists() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);
        agentDaoTestUtils.assertAddNewAgent(testAgent);
    }

    @Test
    public void testUpdateExistingAgentId() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);

        Agent agentToUpdate = (Agent) testAgentDao.selectByAgentId(TEST_AGENT_ID);
        agentToUpdate.setAgentId(UPDATED_TEST_AGENT_ID);

        testAgentDao.update(agentToUpdate);

        Agent receivedAgent = (Agent) testAgentDao.selectByAgentId(UPDATED_TEST_AGENT_ID);

        assertNotNull("Can't find the agent by the updated id. ", receivedAgent);
    }

    @Test(expected = AgentDaoException.class)
    public void testUpdateAgentWhenUpdateFailed() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);
        agentDaoTestUtils.assertAddNewAgent(secondTestAgent);

        Agent agentToUpdate = (Agent) testAgentDao.selectByAgentId(SECOND_TEST_AGENT_ID);
        agentToUpdate.setAgentId(TEST_AGENT_ID);

        testAgentDao.update(agentToUpdate);
    }

    @Test(expected = AgentDaoRuntimeException.class)
    public void testUpdateAgentWhenAgentIsNull() throws Exception {
        Agent agentToUpdate = null;
        testAgentDao.update(agentToUpdate);
    }

    @Test
    public void testRemoveAgentWhichDoesNotExist() throws Exception {
        assertNull("The agent is present when should not exist.", testAgentDao.selectByAgentId(NON_EXISTING_AGENT_ID));

        Agent nonExistingAgent = new Agent(NON_EXISTING_AGENT_ID);
        agentDaoTestUtils.assertRemoveAgent(nonExistingAgent);
    }

    @Test
    public void testSelectAgentByIdWhenAgentExists() throws Exception {
        testAgentDao.add(TEST_AGENT_ID);
        agentDaoTestUtils.assertSelectExistingAgentById(TEST_AGENT_ID);
    }

    @Test
    public void testSelectAgentByIdWhenAgentDoesNotExist() throws Exception {
        agentDaoTestUtils.assertSelectNonExistingAgentById(TEST_AGENT_ID);
    }

    @Test
    public void testGetAgentsWhenThereAreOneAgentOnly() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);
        List<IAgent> receivedAgentsList = testAgentDao.getPresentAgents();

        IAgent receivedAgent = receivedAgentsList.get(0);
        assertEquals("The received agent is different from the one added in the data source.",
                     receivedAgent,
                     testAgent);
    }

    @Test
    public void testGetAgentsWhenThereAreManyAgents() throws Exception {
        agentDaoTestUtils.assertAddNewAgent(testAgent);
        agentDaoTestUtils.assertAddNewAgent(secondTestAgent);
        List<IAgent> expectedAgentsList = new ArrayList<IAgent>();
        expectedAgentsList.add(testAgent);
        expectedAgentsList.add(secondTestAgent);

        List<IAgent> receivedAgentsList = testAgentDao.getPresentAgents();
        assertEquals("The received agent is different from the one added in the data source.",
                     receivedAgentsList,
                     expectedAgentsList);
        assertEquals("The count of the received agents is different from the expected one.",
                     receivedAgentsList.size(),
                     EXPECTED_AGENTS_COUNT);
    }

    @Test
    public void testGetAgentsWhenThereAreNone() throws Exception {
        List<IAgent> receivedAgentsList = testAgentDao.getPresentAgents();
        assertTrue("Received list is not empty when there are no present agents in the data source.",
                   receivedAgentsList.isEmpty());
    }
}
