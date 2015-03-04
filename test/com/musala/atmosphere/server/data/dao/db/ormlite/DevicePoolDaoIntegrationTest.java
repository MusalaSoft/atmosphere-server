package com.musala.atmosphere.server.data.dao.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoRuntimeException;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DeviceDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * 
 * @author denis.bialev
 * 
 */
public class DevicePoolDaoIntegrationTest {

    private static AgentDao testAgentDao;

    private static DeviceDao testDeviceDao;

    private static DevicePoolDao testDevicePoolDao;

    private static DataSourceProvider dataSourceProvider;

    private static ServerEventService eventService;

    private static DataProviderSubscriber dataProviderSubscriber;

    private static Agent[] testAgents = new Agent[2];

    private static final String[] testAgentIds = new String[3];

    private static final String[] testAgentIps = new String[3];

    private static final int[] testAgentPorts = new int[3];

    private static final String NON_EXISTING_AGENT_ID = "non_existing_agent_id";

    private static DeviceInformation[] deviceInformation = new DeviceInformation[2];

    private static Device[] expectedDevices = new Device[2];

    private static final String[] testRmiIds = new String[3];

    private static final long[] testPasskeys = new long[3];

    private static final String NON_EXISTING_RMI_ID = "non-existent";

    private static final int[] testRam = new int[2];

    private static final int NON_EXISTING_RAM = 512;

    private static final int TEST_API_LEVEL = 18;

    private static final int NON_EXISTENT_API_LEVEL = 256;

    private static final String SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE = "The count of the selected device is different than expected.";

    private static void initializeTestConstants() throws AgentDaoException {
        for (int i = 0; i < 3; i++) {
            testAgentIds[i] = "test Agent Id " + i;
            testAgentIps[i] = "test Agent IP " + i;
            testAgentPorts[i] = 1234 + i * 2;
            testRmiIds[i] = "test Rmi Id " + i;
            testPasskeys[i] = 679254132 + i * 5;
            testAgentDao.add(testAgentIds[i], testAgentIps[i], testAgentPorts[i]);
        }

        for (int i = 0; i < 2; i++) {
            testAgents[i] = new Agent(testAgentIds[i]);
            testAgents[i].setHostname(testAgentIps[i]);
            testAgents[i].setPort(testAgentPorts[i]);
            deviceInformation[i] = new DeviceInformation();
            expectedDevices[i] = new Device(deviceInformation[i], testRmiIds[i], testPasskeys[i]);
            expectedDevices[i].setAgent(testAgents[i]);
            testRam[i] = 128 + 128 * i;
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        eventService = new ServerEventService();
        dataSourceProvider = new DataSourceProvider();
        dataProviderSubscriber = new DataProviderSubscriber();

        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);
        eventService.subscribe(AgentDaoCreatedEvent.class, dataProviderSubscriber);
        eventService.subscribe(DeviceDaoCreatedEvent.class, dataProviderSubscriber);
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, dataProviderSubscriber);

        DataSourceManager dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceManager.initialize();

        assertNotNull("Initialization of the database failed. ", testAgentDao);

        initializeTestConstants();
    }

    @After
    public void tearDown() throws Exception {
        for (int i = 0; i < 3; i++) {
            testDeviceDao.remove(testRmiIds[i]);
        }
    }

    @Test
    public void testAddDeviceOneDeviceOnly() throws Exception {
        Device addedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation[0],
                                                                  testRmiIds[0],
                                                                  testAgentIds[0],
                                                                  testPasskeys[0]);
        assertAddDevice(testAgentIds[0], testRmiIds[0], expectedDevices[0], addedDevice);
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testAddDeviceWhenTwoDevicesWithSameIdExists() throws Exception {
        testDevicePoolDao.addDevice(deviceInformation[0], testRmiIds[0], testAgentIds[0], testPasskeys[0]);
        testDevicePoolDao.addDevice(deviceInformation[0], testRmiIds[0], testAgentIds[0], testPasskeys[1]);
    }

    @Test
    public void testAddDeviceTwoDevicesAreWithDifferentAgents() throws Exception {
        Device[] addedDevices = new Device[2];
        for (int i = 0; i < 2; i++) {
            addedDevices[i] = (Device) testDevicePoolDao.addDevice(deviceInformation[i],
                                                                   testRmiIds[i],
                                                                   testAgentIds[i],
                                                                   testPasskeys[i]);
            assertAddDevice(testAgentIds[i], testRmiIds[i], expectedDevices[i], addedDevices[i]);
        }
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testAddDeviceTwoDevicesHaveTheSameIdAndDifferentAgents() throws Exception {
        testDevicePoolDao.addDevice(deviceInformation[0], testRmiIds[0], testAgentIds[0], testPasskeys[0]);
        testDevicePoolDao.addDevice(deviceInformation[1], testRmiIds[0], testAgentIds[1], testPasskeys[1]);
    }

    @Test
    public void testAddDeviceTwoDevicesWithTheSamePasskey() throws Exception {
        Device[] addedDevices = new Device[2];
        for (int i = 0; i < 2; i++) {
            addedDevices[i] = (Device) testDevicePoolDao.addDevice(deviceInformation[i],
                                                                   testRmiIds[i],
                                                                   testAgentIds[i],
                                                                   testPasskeys[0]);
            assertAddDevice(testAgentIds[i], testRmiIds[i], expectedDevices[i], addedDevices[i]);
        }
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testAddDeviceWithNonExistingAgent() throws Exception {
        testDevicePoolDao.addDevice(deviceInformation[0], testRmiIds[0], NON_EXISTING_AGENT_ID, testPasskeys[0]);
    }

    @Test(expected = AgentDaoException.class)
    public void testAddDeviceAndThenRemoveItsAgent() throws Exception {
        Device addedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation[0],
                                                                  testRmiIds[0],
                                                                  testAgentIds[0],
                                                                  testPasskeys[0]);
        assertAddDevice(testAgentIds[0], testRmiIds[0], expectedDevices[0], addedDevice);
        testAgentDao.remove(testAgentIds[0]);
    }

    @Test
    public void testUpdateDeviceOneDeviceWithGivenDeviceInformation() throws Exception {
        Device addedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation[0],
                                                                  testRmiIds[0],
                                                                  testAgentIds[0],
                                                                  testPasskeys[0]);

        DeviceInformation newDeviceInformation = new DeviceInformation();
        newDeviceInformation.setApiLevel(TEST_API_LEVEL);
        newDeviceInformation.setRam(testRam[0]);

        addedDevice.setDeviceInformation(newDeviceInformation);
        testDevicePoolDao.update(addedDevice);

        DeviceInformation updatedDeviceInformation = addedDevice.getInformation();

        assertEquals("The received and the updated device informations are different.",
                     newDeviceInformation,
                     updatedDeviceInformation);
        assertFalse("The added device infromation is not updated as expected.",
                    updatedDeviceInformation.equals(deviceInformation[0]));

        Device selectedDevice = (Device) testDeviceDao.selectById(testRmiIds[0]);
        assertEquals("The added device information is not correctly updated.", addedDevice, selectedDevice);
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testUpdateDeviceRmiIdWhenItIsAlreadyUsed() throws Exception {
        testDevicePoolDao.addDevice(deviceInformation[0], testRmiIds[0], testAgentIds[0], testPasskeys[0]);

        Device addedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation[1],
                                                                  testRmiIds[1],
                                                                  testAgentIds[1],
                                                                  testPasskeys[0]);

        addedDevice.setRmiRegistryId(testRmiIds[0]);
        testDevicePoolDao.update(addedDevice);
    }

    @Test
    public void testGetDeviceById() throws Exception {
        Device addedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation[0],
                                                                  testRmiIds[0],
                                                                  testAgentIds[0],
                                                                  testPasskeys[0]);

        Device receivedDevice = (Device) testDevicePoolDao.getDevice(testRmiIds[0]);
        assertEquals("The added device is different from the received one.", addedDevice, receivedDevice);
    }

    @Test
    public void testGetDeviceByNonExistentId() throws Exception {
        assertNull("A device was fetch using nonexistent id.", testDevicePoolDao.getDevice(testRmiIds[0]));
    }

    @Test
    public void testGetDevicesByDeviceParameters() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        Device firstAddedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation,
                                                                       testRmiIds[0],
                                                                       testAgentIds[0],
                                                                       testPasskeys[0]);

        Device secondAddedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation,
                                                                        testRmiIds[1],
                                                                        testAgentIds[0],
                                                                        testPasskeys[0]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setRam(testRam[0]);
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);

        List<IDevice> devices = testDevicePoolDao.getDevices(deviceParameters);
        assertTrue("A device with the given deviceParameters is not present in the received result list.",
                   devices.contains(firstAddedDevice));
        assertTrue("A device with the given deviceParameters is not present in the received result list.",
                   devices.contains(secondAddedDevice));

    }

    @Test
    public void testGetDevicesWithDifferentAgentsByDeviceParameters() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        Device firstAddedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation,
                                                                       testRmiIds[0],
                                                                       testAgentIds[0],
                                                                       testPasskeys[0]);

        Device secondAddedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation,
                                                                        testRmiIds[1],
                                                                        testAgentIds[1],
                                                                        testPasskeys[0]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setRam(testRam[0]);
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);

        List<IDevice> devices = testDevicePoolDao.getDevices(deviceParameters);
        assertTrue("A device with the given deviceParameters is not present in the given List.",
                   devices.contains(firstAddedDevice));
        assertTrue("A device with the given deviceParameters is not present in the given List.",
                   devices.contains(secondAddedDevice));

    }

    @Test
    public void testGetDevicesWhenNoDevicesAreFound() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        testDevicePoolDao.addDevice(deviceInformation, testRmiIds[0], testAgentIds[0], testPasskeys[0]);
        testDevicePoolDao.addDevice(deviceInformation, testRmiIds[1], testAgentIds[1], testPasskeys[0]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setRam(NON_EXISTING_RAM);
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);

        List<IDevice> devices = testDevicePoolDao.getDevices(deviceParameters);
        assertTrue("Expected empty result list, but instead received list containig devices when there should be none.",
                   devices.isEmpty());
    }

    @Test
    public void testHasDeviceByIdAndDeviceParameters() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        testDevicePoolDao.addDevice(deviceInformation, testRmiIds[0], testAgentIds[0], testPasskeys[0]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setRam(testRam[0]);
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);

        DeviceParameters nonExistentDeviceParameters = new DeviceParameters();
        nonExistentDeviceParameters.setTargetApiLevel(NON_EXISTENT_API_LEVEL);

        assertTrue("The device searched by it's ID is not found when present. ",
                   testDevicePoolDao.hasDevice(testRmiIds[0]));
        assertTrue("The device searched by deviceParameters is not found when present",
                   testDevicePoolDao.hasDevice(deviceParameters));

        assertFalse("A device was found when given non existing ID.", testDevicePoolDao.hasDevice(NON_EXISTING_RMI_ID));
        assertFalse("A device was found when given nonexistent DeviceParameters. ",
                    testDevicePoolDao.hasDevice(nonExistentDeviceParameters));
    }

    @Test
    public void testHasDeviceByIdAndAllocationState() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);
        deviceParameters.setRam(testRam[0]);

        IDevice firstAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[0],
                                                               testAgentIds[0],
                                                               testPasskeys[0]);

        firstAddedDevice.allocate();
        testDevicePoolDao.update(firstAddedDevice);
        assertTrue("Can't find allocated Device when it is present.",
                   testDevicePoolDao.hasDevice(deviceParameters, true));
        assertFalse("Found not allocated Device when it is not present.",
                    testDevicePoolDao.hasDevice(deviceParameters, false));
    }

    @Test
    public void testGetDeviceByDeviceParametersManyDevicesOneMatch() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        DeviceInformation secondDeviceInformation = new DeviceInformation();
        secondDeviceInformation.setApiLevel(TEST_API_LEVEL);
        secondDeviceInformation.setRam(testRam[1]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setRam(testRam[1]);

        IDevice firstAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[0],
                                                               testAgentIds[0],
                                                               testPasskeys[0]);
        IDevice secondAddedDevice = testDevicePoolDao.addDevice(secondDeviceInformation,
                                                                testRmiIds[1],
                                                                testAgentIds[1],
                                                                testPasskeys[1]);
        IDevice thirdAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[2],
                                                               testAgentIds[1],
                                                               testPasskeys[2]);

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);
        assertTrue("The device was not selected successfully by device parameters.",
                   receivedDevices.contains(secondAddedDevice));
        assertFalse("A device with different device information was selected.",
                    receivedDevices.contains(firstAddedDevice));
        assertFalse("A device with different device information was selected.",
                    receivedDevices.contains(thirdAddedDevice));
    }

    @Test
    public void testGetDeviceByDeviceParametersManyDevicesTwoMatches() throws Exception {
        final int EXPECTED_NUMBER_OF_SELECTED_DEVICES = 2;
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        DeviceInformation secondDeviceInformation = new DeviceInformation();
        secondDeviceInformation.setApiLevel(TEST_API_LEVEL);
        secondDeviceInformation.setRam(testRam[1]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setRam(testRam[0]);
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);

        IDevice firstAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[0],
                                                               testAgentIds[0],
                                                               testPasskeys[0]);
        IDevice secondAddedDevice = testDevicePoolDao.addDevice(secondDeviceInformation,
                                                                testRmiIds[1],
                                                                testAgentIds[1],
                                                                testPasskeys[1]);
        IDevice thirdAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[2],
                                                               testAgentIds[1],
                                                               testPasskeys[2]);

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);
        assertTrue("A device was not selected successfully by device parameters.",
                   receivedDevices.contains(firstAddedDevice));
        assertFalse("A device with different device information was selected.",
                    receivedDevices.contains(secondAddedDevice));
        assertTrue("A device was not selected successfully by device parameters.",
                   receivedDevices.contains(thirdAddedDevice));
        assertEquals("Number of received devices is different from the expected one.",
                     receivedDevices.size(),
                     EXPECTED_NUMBER_OF_SELECTED_DEVICES);
    }

    @Test
    public void testGetDevicesWithDeviceParametersAndAllocationState() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        DeviceInformation secondDeviceInformation = new DeviceInformation();
        secondDeviceInformation.setApiLevel(TEST_API_LEVEL);
        secondDeviceInformation.setRam(testRam[1]);

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setTargetApiLevel(TEST_API_LEVEL);

        IDevice firstAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[0],
                                                               testAgentIds[0],
                                                               testPasskeys[0]);
        IDevice secondAddedDevice = testDevicePoolDao.addDevice(secondDeviceInformation,
                                                                testRmiIds[1],
                                                                testAgentIds[1],
                                                                testPasskeys[1]);
        IDevice thirdAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                               testRmiIds[2],
                                                               testAgentIds[0],
                                                               testPasskeys[2]);

        firstAddedDevice.allocate();
        testDevicePoolDao.update(firstAddedDevice);

        secondAddedDevice.release();
        testDevicePoolDao.update(secondAddedDevice);

        thirdAddedDevice.allocate();
        testDevicePoolDao.update(thirdAddedDevice);

        List<IDevice> allocatedDevices = testDevicePoolDao.getDevices(deviceParameters, true);

        assertTrue("An allocated device with the given deviceParameters is not present in the given List.",
                   allocatedDevices.contains(firstAddedDevice));
        assertFalse("The device was present in the list when it is not allocated.",
                    allocatedDevices.contains(secondAddedDevice));
        assertTrue("An allocated device with the given deviceParameters is not present in the given List.",
                   allocatedDevices.contains(thirdAddedDevice));
    }

    @Test
    public void testRemoveDeviceOneDeviceOnly() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        IDevice addedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                          testRmiIds[0],
                                                          testAgentIds[0],
                                                          testPasskeys[0]);
        Agent addedDeviceAgent = (Agent) testAgentDao.selectByAgentId(testAgentIds[0]);
        assertTrue("The agent was not updated after adding a device to it.",
                   addedDeviceAgent.getDevices().contains(addedDevice));
        IDevice selectedDevice = testDeviceDao.selectById(testRmiIds[0]);
        assertEquals("The device added in the database is different from the device reveived from device dao.",
                     addedDevice,
                     selectedDevice);

        testDevicePoolDao.remove(testRmiIds[0]);
        assertNull("A device was found when they were removed.", testDevicePoolDao.getDevice(testRmiIds[0]));
    }

    @Test
    public void testRemoveDeviceWhenDeviceIsAllocated() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        IDevice addedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                          testRmiIds[0],
                                                          testAgentIds[0],
                                                          testPasskeys[0]);
        addedDevice.allocate();
        testDevicePoolDao.update(addedDevice);
        testDevicePoolDao.remove(testRmiIds[0]);
        assertNull("A device was found when they were removed.", testDevicePoolDao.getDevice(testRmiIds[0]));
    }

    @Test
    public void testRemoveDevicesByAgentId() throws Exception {
        final int EXPECTED_NUMBER_OF_REMOVED_DEVICES = 2;
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setApiLevel(TEST_API_LEVEL);
        deviceInformation.setRam(testRam[0]);

        testDevicePoolDao.addDevice(deviceInformation, testRmiIds[0], testAgentIds[0], testPasskeys[0]);
        IDevice secondAddedDevice = testDevicePoolDao.addDevice(deviceInformation,
                                                                testRmiIds[1],
                                                                testAgentIds[0],
                                                                testPasskeys[1]);
        secondAddedDevice.allocate();
        testDevicePoolDao.update(secondAddedDevice);

        testDevicePoolDao.addDevice(deviceInformation, testRmiIds[2], testAgentIds[1], testPasskeys[2]);

        int numberOfRemovedDevices = testDevicePoolDao.removeDevices(testAgentIds[0]);
        assertTrue("The agent was removed after deleting all devices from it.", testAgentDao.hasAgent(testAgentIds[0]));
        assertNull("The Agent's list of devices is not empty when all devices were removed.",
                   testAgents[0].getDevices());
        assertEquals("The number of the removed devices is different from the expected one.",
                     numberOfRemovedDevices,
                     EXPECTED_NUMBER_OF_REMOVED_DEVICES);

        assertFalse("Removing of the device failed.", testDevicePoolDao.hasDevice(testRmiIds[0]));
        assertFalse("Removing of the device failed.", testDevicePoolDao.hasDevice(testRmiIds[1]));
        assertTrue("Device is not present when it was not removed.", testDevicePoolDao.hasDevice(testRmiIds[2]));
    }

    @Test(expected = DevicePoolDaoRuntimeException.class)
    public void testRemoveDevicesByGivenNonExistingAgentId() throws Exception {
        testDevicePoolDao.removeDevices(NON_EXISTING_AGENT_ID);
    }

    @Test
    public void testSelectingRangeOfDevicesByApiLevel() throws Exception {
        populateDeviceBase();

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMinApiLevel(15);
        deviceParameters.setMaxApiLevel(19);
        int expectedDevicesSelected = 1;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());
    }

    @Test
    public void testSelectingDevicesByMinimumApiLevel() throws Exception {
        populateDeviceBase();

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMinApiLevel(16);
        int expectedDevicesSelected = 2;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());
    }

    @Test
    public void testSelectingDevicesByMaximumApiLevel() throws Exception {
        populateDeviceBase();

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMaxApiLevel(21);
        int expectedDevicesSelected = 3;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());
    }

    @Test
    public void testSelectingDevicesByApiLevelNegativeRange() throws Exception {
        populateDeviceBase();

        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMinApiLevel(19);
        deviceParameters.setMaxApiLevel(15);
        int expectedDevicesSelected = 1;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());
    }

    @Test
    public void testSelectingDeviceWhenGivenRangeAndTargetApi() throws Exception {
        populateDeviceBase();

        int EXPECTED_API_LEVEL = 16;
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setTargetApiLevel(EXPECTED_API_LEVEL);
        deviceParameters.setMinApiLevel(9);
        deviceParameters.setMaxApiLevel(21);
        int expectedDevicesSelected = 1;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(deviceParameters);
        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());

        IDevice receivedDevice = receivedDevices.get(0);
        DeviceInformation receivedDeviceInformation = receivedDevice.getInformation();
        assertEquals("The received device is with different API version than the wanted one.",
                     EXPECTED_API_LEVEL,
                     receivedDeviceInformation.getApiLevel());
    }

    @Test
    public void testSelectingDevicesWithTargetApiLevelAndMinimum() throws Exception {
        populateDeviceBase();

        DeviceParameters parameters = new DeviceParameters();
        parameters.setTargetApiLevel(20);
        parameters.setMinApiLevel(19);
        int expectedDevicesSelected = 1;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(parameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());
    }

    @Test
    public void testSelectingDevicesWithTargetApiLevelAndMaximum() throws Exception {
        populateDeviceBase();

        DeviceParameters parameters = new DeviceParameters();
        parameters.setTargetApiLevel(12);
        parameters.setMaxApiLevel(17);
        int expectedDevicesSelected = 2;

        List<IDevice> receivedDevices = testDevicePoolDao.getDevices(parameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevices.size());

        DeviceParameters changedMaxAndTargetParameters = new DeviceParameters();
        changedMaxAndTargetParameters.setTargetApiLevel(16);
        changedMaxAndTargetParameters.setMaxApiLevel(25);
        int expectedDevicesSelecedAfterSetMinAndTagetApiLevel = 1;

        List<IDevice> receivedDevicesTargetApiLevel = testDevicePoolDao.getDevices(changedMaxAndTargetParameters);

        assertEquals(SELECTED_DEVICES_COUNT_DIFFERENT_THAN_EXPECTED_ERROR_MESSAGE,
                     expectedDevicesSelecedAfterSetMinAndTagetApiLevel,
                     receivedDevicesTargetApiLevel.size());
    }

    private void populateDeviceBase() throws Exception {
        DeviceInformation firstDeviceInformation = new DeviceInformation();
        firstDeviceInformation.setApiLevel(10);
        testDevicePoolDao.addDevice(firstDeviceInformation, testRmiIds[0], testAgentIds[0], testPasskeys[0]);

        DeviceInformation secondDeviceInformation = new DeviceInformation();
        secondDeviceInformation.setApiLevel(16);
        testDevicePoolDao.addDevice(secondDeviceInformation, testRmiIds[1], testAgentIds[1], testPasskeys[1]);

        DeviceInformation thirdDeviceInformation = new DeviceInformation();
        thirdDeviceInformation.setApiLevel(21);
        testDevicePoolDao.addDevice(thirdDeviceInformation, testRmiIds[2], testAgentIds[1], testPasskeys[2]);
    }

    /**
     * Asserts that a Device is successfully added in the Database.
     * 
     * @param agentId
     *        - Agent's ID of the added Device
     * @param deviceId
     *        - Device's ID of the added Device
     * @param expectedDevice
     *        - a device that is expected to have been added
     * @param addedDevice
     *        - returned device from adding it to the database
     * @throws Exception
     *         if fetching the device fails.
     */
    public void assertAddDevice(String agentId, String deviceId, Device expectedDevice, Device addedDevice)
        throws Exception {
        String expectedDeviceMessage = "The device, with %s, added in the database is different from the expected one.";
        String expectedDeviceInformationMesasge = "The added device, with %s, has different deviceInformation from the expected one. ";
        String agentUpdatedMessage = "The agent was not updated after adding a device, with %s, to it.";
        String deviceDaoUpdatedMessage = "The device, with %s, added in the database is different from the device reveived from device dao.";

        assertEquals(String.format(expectedDeviceMessage, deviceId), addedDevice, expectedDevice);
        assertEquals(String.format(expectedDeviceInformationMesasge, deviceId),
                     expectedDevice.getInformation(),
                     addedDevice.getInformation());

        Agent addedDeviceAgent = (Agent) testAgentDao.selectByAgentId(agentId);
        assertTrue(String.format(agentUpdatedMessage, deviceId), addedDeviceAgent.getDevices().contains(expectedDevice));
        Device selectedDevice = (Device) testDeviceDao.selectById(deviceId);
        assertEquals(String.format(deviceDaoUpdatedMessage, deviceId), addedDevice, selectedDevice);
        ;
    }

    public static class DataProviderSubscriber implements Subscriber {

        public void inform(AgentDaoCreatedEvent event) {
            testAgentDao = dataSourceProvider.getAgentDao();
        }

        public void inform(DeviceDaoCreatedEvent event) {
            testDeviceDao = dataSourceProvider.getDeviceDao();
        }

        public void inform(DevicePoolDaoCreatedEvent event) {
            testDevicePoolDao = dataSourceProvider.getDevicePoolDao();
        }
    }
}
