package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.data.dao.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.model.IAgent;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.agent.AgentDisconnectedEvent;
import com.musala.atmosphere.server.eventservice.event.agent.AgentEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class ServerManagerIntegrationTest {
    private static final String AGENT_IP = "localhost";

    private static final String DEVICE_RMI_ID_FORMAT = "%s_%s";

    private static final int SERVER_RMI_PORT = 3999;

    private static final String[] AGENT_RMI_IDS = {"first_agent_id", "second_agent_id"};

    private static final int[] AGENT_RMI_PORTS = {1999, 2999};

    private static final String[] DEVICE_RMI_IDS = {"device1_rmi_id", "device2_rmi_id", "device3_rmi_id"};

    private static final String[] DEVICE_SERIAL_NUMBERS = {"device1_serial_number", "device2_serial_number",
            "device3_serial_number"};

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

    private static IAgentManager[] mockedAgentManagers;

    private static Registry[] agentRmiRegistries;

    private static AgentDao agentDao;

    private static DevicePoolDao devicePoolDao;

    @BeforeClass
    public static void setUp() throws Exception {
        mockedAgentManagers = new IAgentManager[AGENT_RMI_IDS.length];
        agentRmiRegistries = new Registry[AGENT_RMI_IDS.length];

        for (int i = 0; i < AGENT_RMI_IDS.length; i++) {
            IAgentManager mockedAgentManager = mock(IAgentManager.class, withSettings().serializable());
            when(mockedAgentManager.getAgentId()).thenReturn(AGENT_RMI_IDS[i]);
            mockedAgentManagers[i] = mockedAgentManager;

            Registry agentRmiRegistry = LocateRegistry.createRegistry(AGENT_RMI_PORTS[i]);
            agentRmiRegistry.rebind(RmiStringConstants.AGENT_MANAGER.toString(), mockedAgentManager);
            agentRmiRegistries[i] = agentRmiRegistry;
        }

        for (int i = 0; i < DEVICE_RMI_IDS.length; i++) {
            IWrapDevice deviceWrapper = mock(IWrapDevice.class, withSettings().serializable());

            DeviceInformation deviceInformation = new DeviceInformation();
            deviceInformation.setSerialNumber(DEVICE_SERIAL_NUMBERS[i]);

            when(deviceWrapper.route(eq(RoutingAction.GET_DEVICE_INFORMATION))).thenReturn(deviceInformation);

            int agentRegistry = i % agentRmiRegistries.length;
            agentRmiRegistries[agentRegistry].rebind(DEVICE_RMI_IDS[i], deviceWrapper);
        }

        eventService = new ServerEventService();
        dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceProvider = new DataSourceProvider();

        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);

        testServerManager = new ServerManager(SERVER_RMI_PORT);
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, testServerManager);
        eventService.subscribe(AgentEvent.class, testServerManager);

        dataSourceManager.initialize();

        agentDao = dataSourceProvider.getAgentDao();
        devicePoolDao = dataSourceProvider.getDevicePoolDao();
    }

    @After
    public void tearDown() throws Exception {
        for (int i = 0; i < AGENT_RMI_IDS.length; i++) {
            if (agentDao.hasAgent(AGENT_RMI_IDS[i])) {
                devicePoolDao.removeDevices(AGENT_RMI_IDS[i]);
                agentDao.remove(AGENT_RMI_IDS[i]);
            }
        }
    }

    @Test
    public void testConnectAgentWhenAgentDoesNotExist() throws Exception {
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());

        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);
        presentAgents = agentDao.getPresentAgents();

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());
    }

    @Test
    public void testConnectAgentWhenAgentAlreadyExists() throws Exception {
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());

        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);
        presentAgents = agentDao.getPresentAgents();

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));
        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);
        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));

        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());
    }

    @Test
    public void testConnectMultipleAgents() throws Exception {
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = AGENT_RMI_IDS.length;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());

        for (int i = 0; i < AGENT_RMI_IDS.length; i++) {
            testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[i]);
        }
        presentAgents = agentDao.getPresentAgents();

        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());
    }

    @Test
    public void testConnectDeviceToAgentWhenAgentIsRegistered() throws Exception {
        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);
        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));

        Agent agent = (Agent) agentDao.selectByAgentId(AGENT_RMI_IDS[0]);
        Collection<Device> devicesOnAgent = agent.getDevices();

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, devicesOnAgent.isEmpty());

        testServerManager.onAgentDeviceListChanged(AGENT_RMI_IDS[0], DEVICE_RMI_IDS[0], true);
        devicesOnAgent = agent.getDevices();

        String deviceIdentifier = getDeviceIdentifier(AGENT_RMI_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        int expectedAttachedDeviceCount = 1;

        assertEquals(DEVICES_COUNT_MISMATCH_ERROR, expectedAttachedDeviceCount, devicesOnAgent.size());
        assertTrue(EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(deviceIdentifier));
    }

    @Test
    public void testConnectDeviceToAgentWhenAgentDoesNotExist() throws Exception {
        assertFalse(NON_EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));

        testServerManager.onAgentDeviceListChanged(AGENT_RMI_IDS[0], DEVICE_RMI_IDS[0], true);

        String deviceIdentifier = getDeviceIdentifier(AGENT_RMI_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        assertFalse(NON_EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(deviceIdentifier));
    }

    @Test
    public void testConnectMultipleDevicesToAgentWhenAgentIsRegistered() throws Exception {
        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));

        Agent agent = (Agent) agentDao.selectByAgentId(AGENT_RMI_IDS[0]);
        Collection<Device> devicesOnAgent = agent.getDevices();
        int expectedAttachedDeviceCount = 2;

        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, devicesOnAgent.isEmpty());

        testServerManager.onAgentDeviceListChanged(AGENT_RMI_IDS[0], DEVICE_RMI_IDS[0], true);
        testServerManager.onAgentDeviceListChanged(AGENT_RMI_IDS[0], DEVICE_RMI_IDS[2], true);

        String firstDeviceIdentifier = getDeviceIdentifier(AGENT_RMI_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        String secondDeviceIdentifier = getDeviceIdentifier(AGENT_RMI_IDS[0], DEVICE_SERIAL_NUMBERS[2]);
        devicesOnAgent = agent.getDevices();

        assertEquals(DEVICES_COUNT_MISMATCH_ERROR, expectedAttachedDeviceCount, devicesOnAgent.size());
        assertTrue(EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(firstDeviceIdentifier));
        assertTrue(EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(secondDeviceIdentifier));
    }

    @Test
    public void testDisconnectAgentWhenNoDeviceIsAttached() throws Exception {
        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());

        eventService.publish(new AgentDisconnectedEvent(mockedAgentManagers[0], AGENT_RMI_IDS[0]));
        presentAgents = agentDao.getPresentAgents();

        assertFalse(NON_EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));
        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());
    }

    @Test
    public void testDisconnectAgentWhenMultipleDevicesAreAttached() throws Exception {
        testServerManager.connectToAgent(AGENT_IP, AGENT_RMI_PORTS[0]);
        List<IAgent> presentAgents = agentDao.getPresentAgents();
        int expectedRegisteredAgentsCount = 1;

        assertTrue(EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));
        assertEquals(AGENTS_COUNT_MISMATCH_ERROR, expectedRegisteredAgentsCount, presentAgents.size());

        testServerManager.onAgentDeviceListChanged(AGENT_RMI_IDS[0], DEVICE_RMI_IDS[0], true);
        testServerManager.onAgentDeviceListChanged(AGENT_RMI_IDS[0], DEVICE_RMI_IDS[2], true);

        String firstDeviceIdentifier = getDeviceIdentifier(AGENT_RMI_IDS[0], DEVICE_SERIAL_NUMBERS[0]);
        String secondDeviceIdentifier = getDeviceIdentifier(AGENT_RMI_IDS[0], DEVICE_SERIAL_NUMBERS[2]);

        eventService.publish(new AgentDisconnectedEvent(mockedAgentManagers[0], AGENT_RMI_IDS[0]));
        presentAgents = agentDao.getPresentAgents();

        assertFalse(NON_EXISTING_AGENT_MISMATCH_ERROR, agentDao.hasAgent(AGENT_RMI_IDS[0]));
        assertFalse(NON_EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(firstDeviceIdentifier));
        assertFalse(NON_EXISTING_DEVICE_MISMATCH_ERROR, devicePoolDao.hasDevice(secondDeviceIdentifier));
        assertTrue(EMPTY_RESULTS_MISMATCH_ERROR, presentAgents.isEmpty());
    }

    private static String getDeviceIdentifier(String onAgentId, String deviceSerialNumber) {
        String deviceIdentifier = String.format(DEVICE_RMI_ID_FORMAT, onAgentId, deviceSerialNumber);

        return deviceIdentifier;
    }
}
