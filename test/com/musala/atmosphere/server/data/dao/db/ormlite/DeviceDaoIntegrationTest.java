package com.musala.atmosphere.server.data.dao.db.ormlite;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.Property;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
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
 * @author delyan.dimitrov
 * 
 */
public class DeviceDaoIntegrationTest implements Subscriber {
    private static final String[] TEST_DEVICE_SERIAL_NUMBERS = {"test1", "test2"};

    private static final String[] TEST_DEVICE_RMI_IDS = {"123", "321"};

    private static final String NOT_INSERTED_DEVICE_ID = "not inserted";

    private static final String TEST_AGENT_ID = "test";

    private static final String TEST_AGENT_IP = "10.0.0.0";

    private static final int TEST_AGENT_PORT = 6000;

    private static final String DEVICE_MISMATCH_ERROR_MESSAGE = "The device retrieved from the data source did not match the expected device.";

    private static final String FAILED_REMOVE_ERROR_MESSAGE = "Query for removed device returned device, when null was expected.";

    private static List<Device> testDevices;

    private static DeviceDao deviceDao;

    private static AgentDao agentDao;

    private static Dao<Device, String> ormliteDeviceDao;

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

        initializeTestDevices();

        // initialize an OrmLite device DAO to clear the device table after each test
        JdbcConnectionSource connectionSource = new JdbcConnectionSource(Property.DATABASE_URL);
        ormliteDeviceDao = DaoManager.createDao(connectionSource, Device.class);

    }

    @After
    public void tearDownTest() throws Exception {
        for (Device device : testDevices) {
            ormliteDeviceDao.delete(device);
        }
    }

    @Test
    public void testAddSingleDevice() throws Exception {
        assumeTrue(deviceDao != null);

        Device testDevice = testDevices.get(0);
        deviceDao.add(testDevice);
        String testDeviceId = testDevice.getDeviceId();

        boolean isExpectedDevice = isDeviceEqualInDataSource(testDeviceId, testDevice);
        assertTrue(DEVICE_MISMATCH_ERROR_MESSAGE, isExpectedDevice);
    }

    @Test
    public void testAddMultipleDevices() throws Exception {
        assumeTrue(deviceDao != null);

        insertAllDevices();

        for (Device testDevice : testDevices) {
            String testDeviceId = testDevice.getDeviceId();

            boolean isExpectedDevice = isDeviceEqualInDataSource(testDeviceId, testDevice);
            assertTrue(DEVICE_MISMATCH_ERROR_MESSAGE, isExpectedDevice);
        }
    }

    @Test
    public void testRemoveSingleDevice() throws Exception {
        assumeTrue(deviceDao != null);

        Device testDevice = testDevices.get(0);
        deviceDao.add(testDevice);

        String testDeviceId = testDevice.getDeviceId();
        deviceDao.remove(testDeviceId);
        Device retrievedDevice = (Device) deviceDao.selectById(testDeviceId);

        assertNull(FAILED_REMOVE_ERROR_MESSAGE, retrievedDevice);
    }

    @Test
    public void testRemoveWhenMultipleDevicesInDataSource() throws Exception {
        assumeTrue(deviceDao != null);

        insertAllDevices();

        Device deviceToRemove = testDevices.get(0);
        String deviceToRemoveId = deviceToRemove.getDeviceId();
        deviceDao.remove(deviceToRemoveId);

        Device removedDevice = (Device) deviceDao.selectById(deviceToRemoveId);
        assertNull(FAILED_REMOVE_ERROR_MESSAGE, removedDevice);

        Device deviceInDataSource = testDevices.get(1);
        String deviceInDataSourceId = deviceInDataSource.getDeviceId();
        boolean isExpectedDevice = isDeviceEqualInDataSource(deviceInDataSourceId, deviceInDataSource);

        assertTrue(DEVICE_MISMATCH_ERROR_MESSAGE, isExpectedDevice);
    }

    @Test
    public void testSelectById() throws Exception {
        assumeTrue(deviceDao != null);

        Device testDevice = testDevices.get(0);
        deviceDao.add(testDevice);

        String testDeviceId = testDevice.getDeviceId();
        Device retrievedDevice = (Device) deviceDao.selectById(testDeviceId);

        boolean isExpectedDevice = retrievedDevice.equals(testDevice);
        assertTrue("Selected device did not match the expected one.", isExpectedDevice);
    }

    @Test
    public void testSelectByIdNoSuchDevice() throws Exception {
        assumeTrue(deviceDao != null);

        Device retrievedDevice = (Device) deviceDao.selectById(NOT_INSERTED_DEVICE_ID);
        assertNull("Received device, when no device with the specified ID is present in the data source.",
                   retrievedDevice);
    }

    @Test(expected = DeviceDaoRuntimeException.class)
    public void testUpdateNullDevice() throws Exception {
        assumeTrue(deviceDao != null);

        deviceDao.update(null);
    }

    @Test
    public void testUpdateWhenMultipleDevicesInDataSource() throws Exception {
        assumeTrue(deviceDao != null);

        insertAllDevices();

        Device deviceToUpdate = testDevices.get(1);
        String deviceToUpdateId = deviceToUpdate.getDeviceId();
        Device deviceToUpdateEntity = (Device) deviceDao.selectById(deviceToUpdateId);

        deviceToUpdateEntity.setCamera(true);
        deviceDao.update(deviceToUpdateEntity);

        boolean isDeviceUpdated = isDeviceEqualInDataSource(deviceToUpdateId, deviceToUpdateEntity);
        assertTrue("Updated device in data source does not match the expected one.", isDeviceUpdated);

        Device notUpdatedDevice = testDevices.get(0);
        String notUpdatedDeviceId = notUpdatedDevice.getDeviceId();
        boolean isDeviceSame = isDeviceEqualInDataSource(notUpdatedDeviceId, notUpdatedDevice);
        assertTrue("Not updated device does not match its initial state.", isDeviceSame);
    }

    private boolean isDeviceEqualInDataSource(String deviceId, Device expectedDevice) {
        Device retrievedDevice;

        try {
            retrievedDevice = (Device) deviceDao.selectById(deviceId);
        } catch (DeviceDaoException e) {
            return false;
        }

        return expectedDevice.equals(retrievedDevice);
    }

    private void insertAllDevices() throws Exception {
        for (Device testDevice : testDevices) {
            deviceDao.add(testDevice);
        }
    }

    private static void initializeTestDevices() throws Exception {
        testDevices = new ArrayList<Device>();
        Agent testAgent = (Agent) agentDao.selectByAgentId(TEST_AGENT_ID);

        for (int i = 0; i < TEST_DEVICE_RMI_IDS.length; ++i) {
            Device testDevice = new Device(TEST_DEVICE_SERIAL_NUMBERS[i], TEST_DEVICE_RMI_IDS[i]);
            testDevice.setAgent(testAgent);
            testDevices.add(testDevice);
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
