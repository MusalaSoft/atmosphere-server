package com.musala.atmosphere.server.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceOs;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.db.ormlite.DeviceDao;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DeviceDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class DeviceDaoSelectionIntegrationTest {
    private static final int[] NONEXISTENT_DEVICE_TARGET_API_LEVELS = {13, 20};

    private static final String[] TEST_DEVICE_SERIAL_NUMBERS = {"emulator", "device1", "device2", "device3"};

    private static final String[] TEST_DEVICE_RMI_IDS = {"rmiId1", "rmiId2", "rmiId3", "rmiId4"};

    private static final int[] TEST_DEVICE_API_LEVELS = {17, 19, 17, 18};

    private static final int[] TEST_DEVICE_RAM_VALUES = {512, 256, 512, 128};

    private static final boolean[] TEST_DEVICE_CAMERA_AVAILABILITY = {false, true, true, true};

    private static final boolean[] TEST_DEVICE_IS_EMULATOR = {true, false, false, false};

    private static final DeviceOs[] TEST_DEVICE_OS = {DeviceOs.JELLY_BEAN_MR1_4_2_1, DeviceOs.KITKAT_4_4,
            DeviceOs.JELLY_BEAN_MR1_4_2_2, DeviceOs.JELLY_BEAN_MR2_4_3};

    private static final String NONEXISTENT_DEVICE_SERIAL_NUMBER = "nonexistent";

    private static final String TEST_AGENT_ID = "test_agent_id";

    private static final String TEST_AGENT_IP = "10.0.0.0";

    private static final int TEST_AGENT_PORT = 6000;

    private static final String EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE = "Expected empty list of results as no device matches the query, but actualy received a non-empty list.";

    private static final String DEVICE_MISMATCH_ERROR_MESSAGE = "Expected real device, but received emulator instead.";

    private static final String EMULATOR_MISMATCH_ERROR_MESSAGE = "Expected emulator, but received real device instead.";

    private static final String DEVICES_LIST_MISMATCH_ERROR_MESSAGE = "The received list count of divices is different than expected.";

    private static final String DEVICE_API_LEVEL_MISMATCH_ERROR_MESSAGE = "The received device has different API level than expected.";

    private static List<Device> testDevices;

    private static Agent testAgent;

    private static DeviceDao deviceDao;

    private static AgentDao agentDao;

    private static DataSourceProvider daoProvider;

    @BeforeClass
    public static void setUpTest() throws Exception {
        ServerEventService eventService = new ServerEventService();

        daoProvider = new DataSourceProvider();
        eventService.subscribe(DataSourceInitializedEvent.class, daoProvider);

        DataSourceEventSubscriber daoInitializer = new DataSourceEventSubscriber();
        eventService.subscribe(DeviceDaoCreatedEvent.class, daoInitializer);
        eventService.subscribe(AgentDaoCreatedEvent.class, daoInitializer);

        DataSourceManager dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceManager.initialize();

        assertNotNull("Initialization of the database failed. ", agentDao);
        assertNotNull("Initialization of the database failed. ", deviceDao);

        // initialize a test agent entry in the data source to attach the devices to
        agentDao.add(TEST_AGENT_ID, TEST_AGENT_IP, TEST_AGENT_PORT);

        testAgent = (Agent) agentDao.selectByAgentId(TEST_AGENT_ID);
        testDevices = new ArrayList<Device>();
        initializeDevices();
    }

    @Test
    public void testFilterDevicesWhenNoSpecificDeviceParametersAreRequired() throws Exception {
        DeviceSelector deviceSelector = new DeviceSelectorBuilder().build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
    }

    @Test
    public void testFilterDevicesWhenMatchingEmulatorOnlyQuery() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.EMULATOR_ONLY);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenMatchingDeviceOnlyQuery() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_ONLY);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);
        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenMatchingDevicePreferredQuery() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_PREFERRED);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenMatchingEmulatorPreferredQuery() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.EMULATOR_PREFERRED);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenNoDeviceWithRequestedOsIsAvailable() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_PREFERRED)
                                                                                 .deviceOs(TEST_DEVICE_OS[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterWhenPreferredTypeDoesNotMatchRequirements() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_PREFERRED)
                                                                                 .isCameraAvailable(TEST_DEVICE_CAMERA_AVAILABILITY[0])
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[0])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterWhenPreferredTypeMatchesRequirements() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_PREFERRED)
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[0])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterAndTypeSpecifiedWhenNoDeviceMatchesRequirements()
        throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_ONLY)
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[1])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[3]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterWhenAvailableResultIsNotFromRequiredType() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_ONLY)
                                                                                 .isCameraAvailable(TEST_DEVICE_CAMERA_AVAILABILITY[0])
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[0])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
    }

    @Test
    public void testFilterDevicesThatAreNotAllocatedWhenNoFreeDeviceIsAvailable() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_PREFERRED)
                                                                                 .isCameraAvailable(TEST_DEVICE_CAMERA_AVAILABILITY[0])
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[0])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        allocateDevices(TEST_DEVICE_SERIAL_NUMBERS[0]);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, isAllocated);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
        releaseDevices(TEST_DEVICE_SERIAL_NUMBERS[0]);
    }

    @Test
    public void testFilterDevicesThatAreNotAllocatedByParametersWhenNoFreeDeviceIsAvailable() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_PREFERRED)
                                                                                 .serialNumber(NONEXISTENT_DEVICE_SERIAL_NUMBER)
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[0])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, isAllocated);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
        releaseDevices(NONEXISTENT_DEVICE_SERIAL_NUMBER);
    }

    @Test
    public void testFilterDevicesThatAreNotAllocatedByParametersWhenNoFreeEmulatorIsAvailable() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.EMULATOR_PREFERRED)
                                                                                 .targetApi(TEST_DEVICE_API_LEVELS[2])
                                                                                 .ramCapacity(TEST_DEVICE_RAM_VALUES[2]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        allocateDevices(TEST_DEVICE_SERIAL_NUMBERS[2]);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, isAllocated);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
        releaseDevices(TEST_DEVICE_SERIAL_NUMBERS[2]);
    }

    @Test
    public void testFilterDevicesWhenNoFreeDevicesAreAvailable() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder();
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        allocateDevices(NONEXISTENT_DEVICE_SERIAL_NUMBER);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, isAllocated);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
        releaseDevices(NONEXISTENT_DEVICE_SERIAL_NUMBER);
    }

    @Test
    public void testFilterDevicesWithGivenRangeAndTargetApiLevel() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().targetApi(TEST_DEVICE_API_LEVELS[3])
                                                                                 .minApi(12)
                                                                                 .maxApi(21);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        int expectedDevicesSelected = 1;

        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());

        IDevice device = receivedDevices.get(0);
        DeviceInformation deviceInformation = device.getInformation();
        assertEquals(DEVICE_API_LEVEL_MISMATCH_ERROR_MESSAGE,
                     TEST_DEVICE_API_LEVELS[3],
                     deviceInformation.getApiLevel());
    }

    @Test
    public void testFilterDevicesWithGivenRangeAndNotFoundDeviceWithTargetApiLevel() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().targetApi(NONEXISTENT_DEVICE_TARGET_API_LEVELS[0])
                                                                                 .minApi(12)
                                                                                 .maxApi(21);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        int expectedDevicesSelected = 4;

        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());
    }

    @Test
    public void testFilterDevicesWithGivenMinimumAndTargetApi() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().targetApi(NONEXISTENT_DEVICE_TARGET_API_LEVELS[1])
                                                                                 .minApi(18);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        int expectedDevicesSelected = 2;

        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());

        deviceSelectorBuilder.minApi(14).maxApi(17).targetApi(TEST_DEVICE_API_LEVELS[0]);
        deviceSelector = deviceSelectorBuilder.build();

        List<IDevice> receivedDevicesChangedTargetApiLevel = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevicesChangedTargetApiLevel.size());
    }

    @Test
    public void testFilterDevicesWithGivenMaximumAndTargetApi() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().maxApi(18)
                                                                                 .targetApi(NONEXISTENT_DEVICE_TARGET_API_LEVELS[0]);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();
        int expectedDevicesSelected = 3;

        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());

        deviceSelectorBuilder.maxApi(21).targetApi(TEST_DEVICE_API_LEVELS[3]);
        deviceSelector = deviceSelectorBuilder.build();
        expectedDevicesSelected = 1;

        List<IDevice> receivedDevicesChangedTargetApiLevel = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevicesChangedTargetApiLevel.size());
    }

    @Test
    public void testFilterDevicesWithGivenRangeForApiLevel() throws Exception {
        DeviceSelectorBuilder deviceSelectorBuilder = new DeviceSelectorBuilder().maxApi(21).minApi(18);
        DeviceSelector deviceSelector = deviceSelectorBuilder.build();

        int expectedDevicesSelected = 2;

        List<IDevice> receivedDevices = deviceDao.filterDevices(deviceSelector, false);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());
    }

    private static void initializeDevices() throws Exception {
        for (int i = 0; i < TEST_DEVICE_RMI_IDS.length; i++) {
            DeviceInformation deviceInformation = new DeviceInformation();

            deviceInformation.setSerialNumber(TEST_DEVICE_SERIAL_NUMBERS[i]);
            deviceInformation.setApiLevel(TEST_DEVICE_API_LEVELS[i]);
            deviceInformation.setOs(TEST_DEVICE_OS[i].toString());
            deviceInformation.setCamera(TEST_DEVICE_CAMERA_AVAILABILITY[i]);
            deviceInformation.setRam(TEST_DEVICE_RAM_VALUES[i]);
            deviceInformation.setCamera(TEST_DEVICE_CAMERA_AVAILABILITY[i]);

            Device device = new Device(deviceInformation, TEST_DEVICE_RMI_IDS[i], i);
            device.setEmulator(TEST_DEVICE_IS_EMULATOR[i]);

            device.setAgent(testAgent);
            testDevices.add(device);
            deviceDao.add(device);
        }
    }

    private void allocateDevices(String exceptDevice) throws Exception {
        for (Device device : testDevices) {
            if (!device.getSerialNumber().equals(exceptDevice)) {
                device.allocate();
                deviceDao.update(device);
            }
        }
    }

    private void releaseDevices(String exceptDevice) throws Exception {
        for (Device device : testDevices) {
            if (!device.getSerialNumber().equals(exceptDevice)) {
                device.release();
                deviceDao.update(device);
            }
        }
    }

    public static class DataSourceEventSubscriber implements Subscriber {
        public void inform(DeviceDaoCreatedEvent event) {
            deviceDao = daoProvider.getDeviceDao();
        }

        public void inform(AgentDaoCreatedEvent event) {
            agentDao = daoProvider.getAgentDao();
        }
    }

}
