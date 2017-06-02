package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.agent.AgentDisconnectedEvent;
import com.musala.atmosphere.server.eventservice.event.agent.AgentEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;

/**
 *
 * @author filareta.yordanova
 *
 */
public class ServerManagerIntegrationTest {
    private static final String DEVICE_RMI_ID_FORMAT = "%s_%s";

    private static final String[] AGENT_IDS = {"first_agent_id", "second_agent_id"};

    private static final String[] DEVICE_SERIAL_NUMBERS = {"device1_serial_number", "device2_serial_number",
            "device3_serial_number"};

    private static DeviceInformation[] deviceInformations = new DeviceInformation[3];

    private static final String EXISTING_AGENT_MISMATCH_ERROR = "The result does not match the expectations, the requested agent is not present.";

    private static final String NON_EXISTING_AGENT_MISMATCH_ERROR = "The result does not match the expectations, requested agent is still present.";

    private static final String EXISTING_DEVICE_MISMATCH_ERROR = "The result does not match the expectations, the requested device is not present.";

    private static final String NON_EXISTING_DEVICE_MISMATCH_ERROR = "The result does not match the expectations, the requested device is still present.";

    private static final String AGENTS_COUNT_MISMATCH_ERROR = "The excpected count of present agents does not match the real one.";

    private static final String DEVICES_COUNT_MISMATCH_ERROR = "The excpected count of present dvices does not match the real one.";

    private static final String EMPTY_RESULTS_MISMATCH_ERROR = "Expectations do not match, result list is not empty.";

    private static DataSourceManager dataSourceManager;

    private static DataSourceProvider dataSourceProvider;

    private static ServerEventService eventService;

    private static ServerManager testServerManager;

    private static AgentDao agentDao;

    private static DevicePoolDao devicePoolDao;

    @BeforeClass
    public static void setUp() throws Exception {
        for (int i = 0; i < DEVICE_SERIAL_NUMBERS.length; i++) {
            DeviceInformation deviceInformation = new DeviceInformation();
            deviceInformation.setSerialNumber(DEVICE_SERIAL_NUMBERS[i]);
            deviceInformations[i] = deviceInformation;
        }

        eventService = new ServerEventService();
        eventService.subscribe(AgentDaoCreatedEvent.class, new AgentAllocator());
        dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceProvider = new DataSourceProvider();

        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);

        testServerManager = new ServerManager();
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, testServerManager);
        eventService.subscribe(AgentEvent.class, testServerManager);

        dataSourceManager.initialize();

        agentDao = dataSourceProvider.getAgentDao();
        devicePoolDao = dataSourceProvider.getDevicePoolDao();
    }

    @After
    public void tearDown() throws Exception {
        for (String agentId : AGENT_IDS) {
            if (agentDao.hasAgent(agentId)) {
                devicePoolDao.removeDevices(agentId);
                agentDao.remove(agentId);
            }
        }
    }

    @Test
    public void testRegisterAgentWhenAgentDoesNotExist() throws Exception {
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());

        testServerManager.registerAgent(AGENT_IDS[0]);
        presentAgents = agentDao.getPresentAgents();

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());
    }

    @Test
    public void testRegisterAgentWhenAgentAlreadyExists() throws Exception {
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());

        testServerManager.registerAgent(AGENT_IDS[0]);
        presentAgents = agentDao.getPresentAgents();

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));

        testServerManager.registerAgent(AGENT_IDS[0]);

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());
    }

    @Test
    public void testRegisterMultipleAgents() throws Exception {
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = AGENT_IDS.length;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());

        for (int i = 0; i < AGENT_IDS.length; i++) {
            testServerManager.registerAgent(AGENT_IDS[i]);
        }
        presentAgents = agentDao.getPresentAgents();

        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());
    }

    @Test
    public void testConnectDeviceToAgentWhenAgentIsRegistered() throws Exception {
        testServerManager.registerAgent(AGENT_IDS[0]);
        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));

        Agent agent = (Agent) agentDao.selectByAgentId(AGENT_IDS[0]);
        Collection<Device> devicesOnAgent = agent.getDevices();

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, devicesOnAgent.isEmpty());

        testServerManager.onAgentDeviceListChanged(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0], deviceInformations[0], true);
        devicesOnAgent = agent.getDevices();

        String deviceIdentifier = getDeviceIdentifier(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        int expectedAttachedDeviceCount = 1;

        assertEquals(DEVICES_COUNT_MISMATCH_ERROR, expectedAttachedDeviceCount, devicesOnAgent.size());
        assertTrue(EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(deviceIdentifier));
    }

    @Test
    public void testConnectDeviceToAgentWhenAgentDoesNotExist() throws Exception {
        assertFalse(NON_EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));

        testServerManager.onAgentDeviceListChanged(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0], deviceInformations[0], true);

        String deviceIdentifier = getDeviceIdentifier(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        assertFalse(NON_EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(deviceIdentifier));
    }

    @Test
    public void testConnectMultipleDevicesToAgentWhenAgentIsRegistered() throws Exception {
        testServerManager.registerAgent(AGENT_IDS[0]);

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));

        Agent agent = (Agent) agentDao.selectByAgentId(AGENT_IDS[0]);
        Collection<Device> devicesOnAgent = agent.getDevices();
        int expectedAttachedDeviceCount = 2;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, devicesOnAgent.isEmpty());

        testServerManager.onAgentDeviceListChanged(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0], deviceInformations[0], true);
        testServerManager.onAgentDeviceListChanged(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[2], deviceInformations[2], true);

        String firstDeviceIdentifier = getDeviceIdentifier(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        String secondDeviceIdentifier = getDeviceIdentifier(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[2]);
        devicesOnAgent = agent.getDevices();

        assertEquals(DEVICES_COUNT_MISMATCH_ERROR, expectedAttachedDeviceCount, devicesOnAgent.size());
        assertTrue(EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(firstDeviceIdentifier));
        assertTrue(EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(secondDeviceIdentifier));
    }

    @Test
    public void testDisconnectAgentWhenNoDeviceIsAttached() throws Exception {
        testServerManager.registerAgent(AGENT_IDS[0]);
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());

        eventService.publish(new AgentDisconnectedEvent(AGENT_IDS[0]));
        presentAgents = agentDao.getPresentAgents();

        assertFalse(NON_EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));
        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());
    }

    @Test
    public void testDisconnectAgentWhenMultipleDevicesAreAttached() throws Exception {
        testServerManager.registerAgent(AGENT_IDS[0]);
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());

        DeviceInformation deviceInformation0 = new DeviceInformation();
        deviceInformation0.setSerialNumber(DEVICE_SERIAL_NUMBERS[0]);

        DeviceInformation deviceInformation2 = new DeviceInformation();
        deviceInformation2.setSerialNumber(DEVICE_SERIAL_NUMBERS[2]);

        testServerManager.onAgentDeviceListChanged(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0], deviceInformations[0], true);
        testServerManager.onAgentDeviceListChanged(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[2], deviceInformations[2], true);

        String firstDeviceIdentifier = getDeviceIdentifier(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        String secondDeviceIdentifier = getDeviceIdentifier(AGENT_IDS[0], DEVICE_SERIAL_NUMBERS[2]);

        eventService.publish(new AgentDisconnectedEvent(AGENT_IDS[0]));
        presentAgents = agentDao.getPresentAgents();

        assertFalse(NON_EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_IDS[0]));
        assertFalse(NON_EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(firstDeviceIdentifier));
        assertFalse(NON_EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(secondDeviceIdentifier));
        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());
    }

    private static String getDeviceIdentifier(String onAgentId, String deviceSerialNumber) {
        String deviceIdentifier = String.format(DEVICE_RMI_ID_FORMAT, onAgentId, deviceSerialNumber);

        return deviceIdentifier;
    }
}
