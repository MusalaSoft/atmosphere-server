package com.musala.atmosphere.server.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.server.data.dao.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.DeviceDao;
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
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class DeviceDaoSelectionIntegrationTest {
    private static final String[] TEST_DEVICE_SERIAL_NUMBERS = {"emulator", "device1", "device2", "device3"};

    private static final String[] TEST_DEVICE_RMI_IDS = {"rmiId1", "rmiId2", "rmiId3", "rmiId4"};

    private static final int[] TEST_DEVICE_API_LEVELS = {17, 19, 17, 18};

    private static final int[] TEST_DEVICE_RAM_VALUES = {512, 256, 512, 128};

    private static final boolean[] TEST_DEVICE_CAMERA_AVAILABILITY = {false, true, true, true};

    private static final boolean[] TEST_DEVICE_IS_EMULATOR = {true, false, false, false};

    private static final DeviceOs[] TEST_DEVICE_OS = {DeviceOs.JELLY_BEAN_MR1_4_2_1, DeviceOs.KITKAT_4_4,
            DeviceOs.JELLY_BEAN_MR1_4_2_2, DeviceOs.JELLY_BEAN_MR2_4_3};

    private static final String UNEXISTING_DEVICE_SERIAL_NUMBER = "unexisting";

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

        // initialize a test agent entry in the data source to attach the devices to
        agentDao.add(TEST_AGENT_ID, TEST_AGENT_IP, TEST_AGENT_PORT);

        testAgent = (Agent) agentDao.selectByAgentId(TEST_AGENT_ID);
        testDevices = new ArrayList<Device>();
        initializeDevices();
    }

    @Test
    public void testFilterDevicesWhenNoSpecificDeviceParametersAreRequired() throws Exception {
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(new DeviceParameters());

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
    }

    @Test
    public void testFilterDevicesWhenMatchingEmulatorOnlyQuery() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.EMULATOR_ONLY);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenMatchingDeviceOnlyQuery() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_ONLY);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);
        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenMatchingDevicePreferredQuery() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_PREFERRED);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenMatchingEmulatorPreferredQuery() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.EMULATOR_PREFERRED);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesWhenNoDeviceWithRequestedOsIsAvailable() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_PREFERRED);
        deviceParameters.setOs(TEST_DEVICE_OS[0]);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterWhenPreferredTypeDoesNotMatchRequirements() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_PREFERRED);
        deviceParameters.setCameraPresent(TEST_DEVICE_CAMERA_AVAILABILITY[0]);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[0]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[0]);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterWhenPreferredTypeMatchesRequirements() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_PREFERRED);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[0]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[0]);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterAndTypeSpecifiedWhenNoDeviceMatchesRequirements()
        throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_ONLY);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[1]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[3]);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
    }

    @Test
    public void testFilterDevicesByMoreThanOneParameterWhenAvailableResultIsNotFromRequiredType() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_ONLY);
        deviceParameters.setCameraPresent(TEST_DEVICE_CAMERA_AVAILABILITY[0]);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[0]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[0]);
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
    }

    @Test
    public void testFilterDevicesThatAreNotAllocatedWhenNoFreeDeviceIsAvailable() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_PREFERRED);
        deviceParameters.setOs(TEST_DEVICE_OS[0]);
        deviceParameters.setCameraPresent(TEST_DEVICE_CAMERA_AVAILABILITY[0]);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[0]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[0]);
        allocateDevices(TEST_DEVICE_SERIAL_NUMBERS[0]);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters, isAllocated);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertTrue(EMULATOR_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
        releaseDevices(TEST_DEVICE_SERIAL_NUMBERS[0]);
    }

    @Test
    public void testFilterDevicesThatAreNotAllocatedByParametersWhenNoFreeDeviceIsAvailable() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.DEVICE_PREFERRED);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[0]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[0]);
        allocateDevices(UNEXISTING_DEVICE_SERIAL_NUMBER);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters, isAllocated);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
        releaseDevices(UNEXISTING_DEVICE_SERIAL_NUMBER);
    }

    @Test
    public void testFilterDevicesThatAreNotAllocatedByParametersWhenNoFreeEmulatorIsAvailable() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.EMULATOR_PREFERRED);
        deviceParameters.setTargetApiLevel(TEST_DEVICE_API_LEVELS[2]);
        deviceParameters.setRam(TEST_DEVICE_RAM_VALUES[2]);
        allocateDevices(TEST_DEVICE_SERIAL_NUMBERS[2]);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters, isAllocated);

        assertFalse(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());

        DeviceInformation resultDeviceInformation = receivedDevices.get(0).getInformation();
        assertFalse(DEVICE_MISMATCH_ERROR_MESSAGE, resultDeviceInformation.isEmulator());
        releaseDevices(TEST_DEVICE_SERIAL_NUMBERS[2]);
    }

    @Test
    public void testFilterDevicesWithNoPreferenceWhenNoFreeDevicesAreAvailable() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setDeviceType(DeviceType.NO_PREFERENCE);
        allocateDevices(UNEXISTING_DEVICE_SERIAL_NUMBER);

        boolean isAllocated = false;
        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters, isAllocated);

        assertTrue(EMPTY_RESULT_LIST_MISMATCH_ERROR_MESSAGE, receivedDevices.isEmpty());
        releaseDevices(UNEXISTING_DEVICE_SERIAL_NUMBER);
    }

    @Test
    public void testFilterDevicesWithGivenRangeAndTargetApiLevel() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMinApiLevel(12);
        deviceParameters.setMaxApiLevel(21);
        deviceParameters.setTargetApiLevel(18);
        int expectedDevicesSelected = 1;

        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());

        IDevice device = receivedDevices.get(0);
        DeviceInformation deviceInformation = device.getInformation();
        assertEquals(DEVICE_API_LEVEL_MISMATCH_ERROR_MESSAGE, 18, deviceInformation.getApiLevel());
    }

    @Test
    public void testFilterDevicesWithGivenRangeAndNotFoundDeviceWithTargetApiLevel() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMinApiLevel(12);
        deviceParameters.setMaxApiLevel(21);
        deviceParameters.setTargetApiLevel(12);
        int expectedDevicesSelected = 4;

        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());
    }

    @Test
    public void testFilterDevicesWithGivenMinimumAndTargetApi() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMinApiLevel(18);
        deviceParameters.setTargetApiLevel(20);

        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, 2, receivedDevices.size());

        deviceParameters.setMinApiLevel(14);
        deviceParameters.setTargetApiLevel(17);
        int expectedDevicesSelected = 2;

        List<IDevice> receivedDevicesChangedTargetApiLevel = deviceDao.filterDevicesByParameters(deviceParameters);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE,
                     expectedDevicesSelected,
                     receivedDevicesChangedTargetApiLevel.size());
    }

    @Test
    public void testFilterDevicesWithGivenMaximumAndTargetApi() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMaxApiLevel(18);
        deviceParameters.setTargetApiLevel(14);
        int expectedDevicesSelected = 3;

        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, expectedDevicesSelected, receivedDevices.size());

        deviceParameters.setMaxApiLevel(21);
        deviceParameters.setTargetApiLevel(18);

        List<IDevice> receivedDevicesChangedTargetApiLevel = deviceDao.filterDevicesByParameters(deviceParameters);

        assertEquals(DEVICES_LIST_MISMATCH_ERROR_MESSAGE, 1, receivedDevicesChangedTargetApiLevel.size());
    }

    @Test
    public void testFilterDevicesWithGivenRangeForApiLevel() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setMaxApiLevel(21);
        deviceParameters.setMinApiLevel(18);
        int expectedDevicesSelected = 2;

        List<IDevice> receivedDevices = deviceDao.filterDevicesByParameters(deviceParameters);

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
