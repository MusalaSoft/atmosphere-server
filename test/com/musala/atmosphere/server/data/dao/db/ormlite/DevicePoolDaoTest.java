package com.musala.atmosphere.server.data.dao.db.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoRuntimeException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class DevicePoolDaoTest {
    private static final String TEST_AGENT_ID = "agent_id";

    private static final String EXISTING_AGENT_ID = "existing_agent_id";

    private static final String TEST_DEVICE_SERIAL_NUMBER = "serial_number";

    private static final String TEST_DEVICE_RMI_ID = "rmi_registry_id";

    private static final long TEST_DEVICE_PASSKEY = 123456;

    private static final String EXISTING_DEVICE_SERIAL_NUMBER = "existing_serial_number";

    private static final String EXISTING_DEVICE_RMI_ID = "existing_rmi_registry_id";

    private static AgentDao mockedAgentDao;

    private static DeviceDao mockedDeviceDao;

    private static Agent mockedAgent;

    private static DevicePoolDao testDevicePoolDao;

    @Before
    public void setUp() throws Exception {
        mockedDeviceDao = mock(DeviceDao.class);
        mockedAgentDao = mock(AgentDao.class);
        mockedAgent = mock(Agent.class);
        testDevicePoolDao = new DevicePoolDao(mockedDeviceDao, mockedAgentDao);
    }

    @Test
    public void testAddingNewDevice() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setSerialNumber(TEST_DEVICE_SERIAL_NUMBER);
        Device expectedDevice = new Device(deviceInformation, TEST_DEVICE_RMI_ID, TEST_DEVICE_PASSKEY);
        expectedDevice.setAgent(mockedAgent);

        when(mockedAgentDao.selectByAgentId(eq(EXISTING_AGENT_ID))).thenReturn(mockedAgent);

        Device addedDevice = (Device) testDevicePoolDao.addDevice(deviceInformation,
                                                                  TEST_DEVICE_RMI_ID,
                                                                  EXISTING_AGENT_ID,
                                                                  TEST_DEVICE_PASSKEY);
        verify(mockedDeviceDao, times(1)).add(eq(expectedDevice));

        assertEquals("Added device is different from the expected one.", expectedDevice, addedDevice);
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testAddingNewDeviceWhenAgentForRequestedIdDoesNotExist() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setSerialNumber(TEST_DEVICE_SERIAL_NUMBER);
        Device expectedDevice = new Device(deviceInformation, TEST_DEVICE_RMI_ID, TEST_DEVICE_PASSKEY);
        Agent testAgent = null;
        expectedDevice.setAgent(testAgent);

        when(mockedAgentDao.selectByAgentId(eq(TEST_AGENT_ID))).thenReturn(testAgent);
        doThrow(new DeviceDaoException("Agent for this device is missing!")).when(mockedDeviceDao)
                                                                            .add(eq(expectedDevice));

        testDevicePoolDao.addDevice(deviceInformation, TEST_DEVICE_RMI_ID, TEST_AGENT_ID, TEST_DEVICE_PASSKEY);

        verify(mockedDeviceDao, times(1)).add(eq(expectedDevice));
    }

    @Test
    public void testGetDeviceWhichExists() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setSerialNumber(EXISTING_DEVICE_SERIAL_NUMBER);
        Device expectedDevice = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_RMI_ID);
        expectedDevice.setAgent(mockedAgent);

        when(mockedDeviceDao.selectById(eq(EXISTING_DEVICE_RMI_ID))).thenReturn(expectedDevice);

        Device device = (Device) testDevicePoolDao.getDevice(EXISTING_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).selectById(eq(EXISTING_DEVICE_RMI_ID));

        assertEquals("Getting device by ID returned result different from the expected.", expectedDevice, device);
    }

    @Test
    public void testGetDeviceWhichDoesNotExist() throws Exception {
        Device expectedDevice = null;

        when(mockedDeviceDao.selectById(eq(TEST_DEVICE_RMI_ID))).thenReturn(expectedDevice);

        Device device = (Device) testDevicePoolDao.getDevice(TEST_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).selectById(eq(TEST_DEVICE_RMI_ID));

        assertNull("Expected that device with the requested RMI id would not be found.", device);
    }

    @Test
    public void testHasDeviceWithExistingId() throws Exception {
        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setSerialNumber(EXISTING_DEVICE_SERIAL_NUMBER);
        Device expectedDevice = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_RMI_ID);
        expectedDevice.setAgent(mockedAgent);

        when(mockedDeviceDao.selectById(eq(EXISTING_DEVICE_RMI_ID))).thenReturn(expectedDevice);

        boolean isDeviceFound = testDevicePoolDao.hasDevice(EXISTING_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).selectById(eq(EXISTING_DEVICE_RMI_ID));

        assertTrue("Device with the requested RMI id was not found.", isDeviceFound);
    }

    @Test
    public void testHasDeviceWithUnexistingId() throws Exception {
        Device expectedDevice = null;

        when(mockedDeviceDao.selectById(eq(TEST_DEVICE_RMI_ID))).thenReturn(expectedDevice);

        boolean isDeviceFound = testDevicePoolDao.hasDevice(TEST_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).selectById(eq(TEST_DEVICE_RMI_ID));

        assertFalse("Expected that device matching the requested RMI id would not be found.", isDeviceFound);
    }

    @Test
    public void testRemoveDevicesOnExisitngAgent() throws Exception {
        Device attachedDevice = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        attachedDevice.setAgent(mockedAgent);

        Collection<Device> devicesOnAgent = new ArrayList<Device>();
        devicesOnAgent.add(attachedDevice);
        int expectedRemovedCount = devicesOnAgent.size();

        when(mockedAgent.getDevices()).thenReturn(devicesOnAgent);
        when(mockedAgentDao.selectByAgentId(eq(TEST_AGENT_ID))).thenReturn(mockedAgent);

        int removedCount = testDevicePoolDao.removeDevices(TEST_AGENT_ID);

        verify(mockedDeviceDao, times(expectedRemovedCount)).remove(eq(TEST_DEVICE_RMI_ID));

        assertEquals("The actual removed count is different from the expected one.", expectedRemovedCount, removedCount);
    }

    @Test(expected = DevicePoolDaoRuntimeException.class)
    public void testRemoveDevicesWhenAgentIsMissing() throws Exception {
        Device attachedDevice = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        Agent missingAgent = null;
        attachedDevice.setAgent(missingAgent);

        Collection<Device> devicesOnAgent = new ArrayList<Device>();
        devicesOnAgent.add(attachedDevice);

        when(mockedAgent.getDevices()).thenReturn(devicesOnAgent);
        when(mockedAgentDao.selectByAgentId(eq(TEST_AGENT_ID))).thenReturn(missingAgent);

        testDevicePoolDao.removeDevices(TEST_AGENT_ID);

        verify(mockedDeviceDao, times(0)).remove(eq(TEST_DEVICE_RMI_ID));
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testRemoveDevicesWhenSelectingAgentFails() throws Exception {
        Device attachedDevice = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        attachedDevice.setAgent(mockedAgent);

        Collection<Device> devicesOnAgent = new ArrayList<Device>();
        devicesOnAgent.add(attachedDevice);

        when(mockedAgent.getDevices()).thenReturn(devicesOnAgent);
        when(mockedAgentDao.selectByAgentId(eq(TEST_AGENT_ID))).thenThrow(new AgentDaoException("Agent was not found."));

        testDevicePoolDao.removeDevices(TEST_AGENT_ID);

        verify(mockedDeviceDao, times(0)).remove(eq(TEST_DEVICE_RMI_ID));
    }

    @Test
    public void testRemoveDevicesWhenRemovingCertainDeviceFails() throws Exception {
        Device attachedDevice = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        Device failedAttachedDevice = new Device(EXISTING_DEVICE_SERIAL_NUMBER, EXISTING_DEVICE_RMI_ID);
        attachedDevice.setAgent(mockedAgent);
        failedAttachedDevice.setAgent(mockedAgent);

        Collection<Device> devicesOnAgent = new ArrayList<Device>();
        devicesOnAgent.add(attachedDevice);
        devicesOnAgent.add(failedAttachedDevice);
        int expectedRemovedCount = devicesOnAgent.size();

        when(mockedAgent.getDevices()).thenReturn(devicesOnAgent);
        when(mockedAgentDao.selectByAgentId(eq(TEST_AGENT_ID))).thenReturn(mockedAgent);
        doThrow(new DeviceDaoException("Device was not removed!")).when(mockedDeviceDao).remove(eq(TEST_DEVICE_RMI_ID));

        int removedCount = testDevicePoolDao.removeDevices(TEST_AGENT_ID);

        verify(mockedDeviceDao, times(expectedRemovedCount)).remove(any(String.class));
        assertEquals("The actual removed count is different from the expected one.",
                     expectedRemovedCount - 1,
                     removedCount);
    }

    @Test
    public void testGetDevicesByDeviceParameters() throws Exception {
        Device attachedDevice = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        attachedDevice.setAgent(mockedAgent);
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setSerialNumber(TEST_DEVICE_SERIAL_NUMBER);

        List<IDevice> expectedResultList = new ArrayList<IDevice>();
        expectedResultList.add(attachedDevice);

        when(mockedDeviceDao.filterDevicesByParameters(eq(deviceParameters), eq(false))).thenReturn(expectedResultList);
        List<IDevice> actualResultList = testDevicePoolDao.getDevices(deviceParameters);

        assertEquals("Devices found by the given device parameters do not match the expected result.",
                     expectedResultList,
                     actualResultList);
    }

    @Test
    public void testGetDevicesByDeviceParametersWhenNoMatchingDevices() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setSerialNumber(TEST_DEVICE_SERIAL_NUMBER);

        List<IDevice> resultList = new ArrayList<IDevice>();

        when(mockedDeviceDao.filterDevicesByParameters(eq(deviceParameters), eq(false))).thenReturn(resultList);
        List<IDevice> actualResultList = testDevicePoolDao.getDevices(deviceParameters);

        assertTrue("Expected that no devices matching the requested parameters would be found.",
                   actualResultList.isEmpty());
    }

    @Test
    public void testHasDevicesByDeviceParametersWhenNoMatchingDevices() throws Exception {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setSerialNumber(TEST_DEVICE_SERIAL_NUMBER);

        List<IDevice> resultList = new ArrayList<IDevice>();

        when(mockedDeviceDao.filterDevicesByParameters(eq(deviceParameters), eq(false))).thenReturn(resultList);
        boolean isDeviceFound = testDevicePoolDao.hasDevice(deviceParameters);

        assertFalse("Expected that device matching the requested parameters would not be found.", isDeviceFound);
    }

    @Test
    public void testHasDevicesByDeviceParameters() throws Exception {
        Device attachedDevice = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        attachedDevice.setAgent(mockedAgent);
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setSerialNumber(TEST_DEVICE_SERIAL_NUMBER);

        List<IDevice> expectedResultList = new ArrayList<IDevice>();
        expectedResultList.add(attachedDevice);

        when(mockedDeviceDao.filterDevicesByParameters(eq(deviceParameters), eq(false))).thenReturn(expectedResultList);
        boolean isDeviceFound = testDevicePoolDao.hasDevice(deviceParameters);

        assertTrue("Finding device for the requested parameters failed.", isDeviceFound);
    }

    @Test
    public void testRemoveExistingDevice() throws Exception {
        testDevicePoolDao.remove(TEST_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).remove(eq(TEST_DEVICE_RMI_ID));
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testRemoveDeviceWhenDeletetingFails() throws Exception {
        doThrow(new DeviceDaoException("Removing device failed!")).when(mockedDeviceDao).remove(eq(TEST_DEVICE_RMI_ID));

        testDevicePoolDao.remove(TEST_DEVICE_RMI_ID);
        verify(mockedDeviceDao, times(1)).remove(eq(TEST_DEVICE_RMI_ID));
    }

    @Test
    public void testUpdateExistingDevice() throws Exception {
        Device device = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        device.setAgent(mockedAgent);

        testDevicePoolDao.update(device);
        verify(mockedDeviceDao, times(1)).update(eq(device));
    }

    @Test(expected = DevicePoolDaoException.class)
    public void testUpdateDeviceWhenUpdateInDataSourceFails() throws Exception {
        Device device = new Device(TEST_DEVICE_SERIAL_NUMBER, TEST_DEVICE_RMI_ID);
        device.setAgent(mockedAgent);

        doThrow(new DeviceDaoException("Updating device failed!")).when(mockedDeviceDao).update(eq(device));

        testDevicePoolDao.update(device);
        verify(mockedDeviceDao, times(1)).update(eq(device));
    }
}
