package com.musala.atmosphere.server.data.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Class containing assertions and other useful methods implementing logic for the {@link AgentDao} integration tests.
 *
 * @author denis.bialev
 *
 */
public class AgentDaoTestUtils {

    private static AgentDao testAgentDao;

    private static DataSourceProvider dataSourceProvider;

    private static ServerEventService eventService;

    private static AgentDaoFakeSubscriber fakeSubscriber;

    public static class AgentDaoFakeSubscriber implements Subscriber {

        public void inform(AgentDaoCreatedEvent event) {
            testAgentDao = dataSourceProvider.getAgentDao();
        }
    }

    /**
     * Initializes the database and creates an {@link AgentDao} instance.
     *
     * @return <@link AgentDao> that manages the table of the agents
     */
    public void setUpDatabase() {
        eventService = new ServerEventService();
        dataSourceProvider = new DataSourceProvider();

        fakeSubscriber = new AgentDaoFakeSubscriber();

        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);
        eventService.subscribe(AgentDaoCreatedEvent.class, fakeSubscriber);

        DataSourceManager dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceManager.initialize();
    }

    public AgentDao getAgentDao() {
        return testAgentDao;
    }

    /**
     * Asserts that adding of the given agent, in the database, is successful.
     *
     * @param agentToAdd
     *        - the agent that will be added in the database.
     * @throws AgentDaoException
     *         if adding the agent fails
     */
    public void assertAddNewAgent(Agent agentToAdd) throws AgentDaoException {
        testAgentDao.add(agentToAdd.getAgentId(), agentToAdd.getHostname(), agentToAdd.getPort());
        Agent receivedAgent = (Agent) testAgentDao.selectByAgentId(agentToAdd.getAgentId());

        assertEquals("Expected agent is different than the received one.", agentToAdd, receivedAgent);
    }

    /**
     * Asserts that removing of the given agent, from the database, is successful.
     *
     * @param agentToRemove
     *        - the agent that will be removed from the database
     * @throws AgentDaoException
     *         when selecting the agent from the database fails
     */
    public void assertRemoveAgent(Agent agentToRemove) throws AgentDaoException {
        testAgentDao.remove(agentToRemove.getAgentId());

        Agent receivedAgent = (Agent) testAgentDao.selectByAgentId(agentToRemove.getAgentId());

        assertNull("The agent is present when there should be no such agent in the database.", receivedAgent);
    }

    /**
     * Asserts that selecting of an agent that is present in the database is successful.
     *
     * @param testAgentId
     *        - id of the agent that will be selected
     * @throws AgentDaoException
     *         when selecting the agent from the database fails
     */
    public void assertSelectExistingAgentById(String testAgentId) throws AgentDaoException {
        Agent receivedAgent = (Agent) testAgentDao.selectByAgentId(testAgentId);

        assertEquals("The returned agent with the given ID does not match the expected one.",
                     testAgentId,
                     receivedAgent.getAgentId());
    }

    /**
     * Asserts that selecting of an agent that is not present in the database returns <code>null</code>.
     *
     * @param testAgentId
     *        - id of the agent that will be selected
     * @throws AgentDaoException
     *         when selecting the agent from the database fails
     */
    public void assertSelectNonExistingAgentById(String testAgentId) throws AgentDaoException {
        Agent receivedAgent = (Agent) testAgentDao.selectByAgentId(testAgentId);

        assertNull("Returned an agent that should not be present in the database.", receivedAgent);
    }

    /**
     * Unsubscribes the fake subscribers.
     */
    public void unsubscribe() {
        eventService.unsubscribe(AgentDaoCreatedEvent.class, null, fakeSubscriber);
        eventService.unsubscribe(DataSourceInitializedEvent.class, null, dataSourceProvider);
    }
}
