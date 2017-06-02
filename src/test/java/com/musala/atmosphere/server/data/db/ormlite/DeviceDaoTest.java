package com.musala.atmosphere.server.data.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
import com.musala.atmosphere.server.data.db.ormlite.DeviceDao;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class DeviceDaoTest {
    private static final String TEST_DEVICE_SERIAL_NUMBER = "serial_number";

    private static final String TEST_DEVICE_RMI_ID = "device_id";

    private static final String EXISTING_DEVICE_SERIAL_NUMBER = "existing_serial_number";

    private static final String EXISTING_DEVICE_ID = "existing_device_id";

    private static DeviceDao testDeviceDao;

    private static Dao<Device, String> mockedDeviceDao;

    private static Agent mockedAgent;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        mockedDeviceDao = mock(Dao.class);
        mockedAgent = mock(Agent.class);
        testDeviceDao = new DeviceDao(mockedDeviceDao);
    }

    @Test
    public void testAddNewDevice() throws Exception {
        Device device = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        device.setAgent(mockedAgent);

        when(mockedDeviceDao.create(eq(device))).thenReturn(1);

        testDeviceDao.add(device);
        verify(mockedDeviceDao, times(1)).create(eq(device));
    }

    @Test
    public void testRemoveDeviceWhenDeviceExists() throws Exception {
        List<Device> resultsList = getFakeResultList();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(DeviceColumnName.DEVICE_ID, EXISTING_DEVICE_ID);
        Device deviceToDelete = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_ID);
        deviceToDelete.setAgent(mockedAgent);

        when(mockedDeviceDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);
        when(mockedDeviceDao.delete(eq(deviceToDelete))).thenReturn(1);

        testDeviceDao.remove(EXISTING_DEVICE_ID);
        verify(mockedDeviceDao, times(1)).delete(eq(deviceToDelete));
    }

    @Test(expected = DeviceDaoException.class)
    public void testAddDeviceThatAlreadyExists() throws Exception {
        Device device = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_ID);
        device.setAgent(mockedAgent);

        when(mockedDeviceDao.create(eq(device))).thenThrow(new SQLException());

        testDeviceDao.add(device);
        verify(mockedDeviceDao, times(1)).create(eq(device));
    }

    @Test
    public void testUpdateExistingDevice() throws Exception {
        Device deviceToUpdate = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        deviceToUpdate.setAgent(mockedAgent);

        when(mockedDeviceDao.update(eq(deviceToUpdate))).thenReturn(1);

        testDeviceDao.update(deviceToUpdate);
        verify(mockedDeviceDao, times(1)).update(eq(deviceToUpdate));
    }

    @Test(expected = DeviceDaoException.class)
    public void testUpdateDeviceWhenUpdateFailed() throws Exception {
        Device deviceToUpdate = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        deviceToUpdate.setAgent(mockedAgent);

        when(mockedDeviceDao.update(eq(deviceToUpdate))).thenThrow(new SQLException());

        testDeviceDao.update(deviceToUpdate);
        verify(mockedDeviceDao, times(1)).update(eq(deviceToUpdate));
    }

    @Test(expected = DeviceDaoException.class)
    public void testRemoveDeviceWhenDeleteFailed() throws Exception {
        List<Device> resultsList = getFakeResultList();
        Device deviceToRemove = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_ID);
        deviceToRemove.setAgent(mockedAgent);
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(DeviceColumnName.DEVICE_ID, EXISTING_DEVICE_ID);

        when(mockedDeviceDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);
        when(mockedDeviceDao.delete(eq(deviceToRemove))).thenThrow(new SQLException());

        testDeviceDao.remove(EXISTING_DEVICE_ID);
        verify(mockedDeviceDao, times(1)).delete(eq(deviceToRemove));
    }

    @Test
    public void testRemoveDeviceWhichDoesNotExist() throws Exception {
        List<Device> resultsList = new ArrayList<Device>();
        Device deviceToRemove = null;
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(DeviceColumnName.DEVICE_ID, TEST_DEVICE_RMI_ID);
        when(mockedDeviceDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        testDeviceDao.remove(TEST_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).queryForFieldValuesArgs(eq(query));
        verify(mockedDeviceDao, times(0)).delete(eq(deviceToRemove));
    }

    @Test(expected = DeviceDaoRuntimeException.class)
    public void testAddDeviceWhichIsNull() throws Exception {
        Device deviceToAdd = null;

        testDeviceDao.add(deviceToAdd);
        verify(mockedDeviceDao, times(0)).create(eq(deviceToAdd));
    }

    @Test(expected = DeviceDaoRuntimeException.class)
    public void testUpdateDeviceWhichIsNull() throws Exception {
        Device deviceToUpdate = null;

        testDeviceDao.update(deviceToUpdate);
        verify(mockedDeviceDao, times(0)).update(eq(deviceToUpdate));
    }

    @Test
    public void testSelectDeviceByRmiId() throws Exception {
        List<Device> resultsList = getFakeResultList();
        Device expectedDevice = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_ID);
        expectedDevice.setAgent(mockedAgent);
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(DeviceColumnName.DEVICE_ID, EXISTING_DEVICE_ID);

        when(mockedDeviceDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        IDevice receivedDevice = testDeviceDao.selectById(EXISTING_DEVICE_ID);
        verify(mockedDeviceDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertEquals("Device found for the requested RMI id is different from the expected one.",
                     expectedDevice,
                     receivedDevice);
    }

    @Test
    public void testSelectByRmiIdWhenDeviceDoesNotExist() throws Exception {
        List<Device> resultsList = new ArrayList<Device>();
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(DeviceColumnName.DEVICE_ID, TEST_DEVICE_RMI_ID);

        when(mockedDeviceDao.queryForFieldValuesArgs(eq(query))).thenReturn(resultsList);

        IDevice receivedDevice = testDeviceDao.selectById(TEST_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).queryForFieldValuesArgs(eq(query));

        assertNull("Find a device matching the requested RMI id, even though it does not exist.", receivedDevice);
    }

    private List<Device> getFakeResultList() {
        List<Device> resultsList = new ArrayList<Device>();
        Device existingDevice = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_ID);
        existingDevice.setAgent(mockedAgent);

        resultsList.add(existingDevice);

        return resultsList;
    }

}
