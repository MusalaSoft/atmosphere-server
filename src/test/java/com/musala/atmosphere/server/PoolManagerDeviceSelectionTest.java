// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceOs;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.data.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.pool.PoolManager;

@RunWith(MockitoJUnitRunner.class)
public class PoolManagerDeviceSelectionTest {
    private final static String BUILD_DEVICE_IDENTIFIER_METHOD_NAME = "buildDeviceIdentifier";

    private final static String AGENT_ID = "mockagent";
    
    private static final String DEVICE_ID_FORMAT = AGENT_ID + "_%s";

    private final static String FIRST_DEVICE_SERIAL_NUMBER = "mockdevice1";

    private final static String SECOND_DEVICE_SERIAL_NUMBER = "mockdevice2";

    private final static String THIRD_DEVICE_SERIAL_NUMBER = "mockdevice3";

    private final static String FOURTH_DEVICE_SERIAL_NUMBER = "mockdevice4";

    private static final String FIFTH_DEVICE_SERIAL_NUMBER = "mockdevice5";

    private final static String FIRST_DEVICE_ID = String.format(DEVICE_ID_FORMAT, FIRST_DEVICE_SERIAL_NUMBER);

    private final static String SECOND_DEVICE_ID = String.format(DEVICE_ID_FORMAT, SECOND_DEVICE_SERIAL_NUMBER);

    private final static String THIRD_DEVICE_ID = String.format(DEVICE_ID_FORMAT, THIRD_DEVICE_SERIAL_NUMBER);

    private final static String FOURTH_DEVICE_ID = String.format(DEVICE_ID_FORMAT, FOURTH_DEVICE_SERIAL_NUMBER);

    private static final String FIFTH_DEVICE_ID = String.format(DEVICE_ID_FORMAT, FIFTH_DEVICE_SERIAL_NUMBER);

    private static final long FIRST_DEVICE_PASSKEY = 1;

    private static final long SECOND_DEVICE_PASSKEY = 2;

    private static final long THIRD_DEVICE_PASSKEY = 3;

    private static final long FOURTH_DEVICE_PASSKEY = 4;

    private static final long FIFTH_DEVICE_PASSKEY = 5;

    private static IDevice firstDevice;

    private static IDevice secondDevice;

    private static IDevice thirdDevice;

    private static IDevice fourthDevice;

    private static IDevice fifthDevice;

    private static List<IDevice> deviceList;

    private static Agent mockedAgent;

    @InjectMocks
    private static PoolManager poolManager = PoolManager.getInstance();

    @Mock
    private static DevicePoolDao devicePoolDao;

    @BeforeClass
    public static void setUp() throws Exception {
        mockedAgent = mock(Agent.class);
        when(mockedAgent.getAgentId()).thenReturn(AGENT_ID);

        devicePoolDao = mock(DevicePoolDao.class);
    }

    @Before
    public void setUpTest() throws Exception {
        DeviceInformation mockedDeviceInfoOne = new DeviceInformation();
        mockedDeviceInfoOne.setSerialNumber(FIRST_DEVICE_SERIAL_NUMBER);
        mockedDeviceInfoOne.setOs("4.2.1");
        mockedDeviceInfoOne.setEmulator(true);
        mockedDeviceInfoOne.setRam(128);
        mockedDeviceInfoOne.setResolution(new Pair<>(600, 800));
        mockedDeviceInfoOne.setDpi(120);

        DeviceInformation mockedDeviceInfoTwo = new DeviceInformation();
        mockedDeviceInfoTwo.setSerialNumber(SECOND_DEVICE_SERIAL_NUMBER);
        mockedDeviceInfoTwo.setOs("4.1.1");
        mockedDeviceInfoTwo.setEmulator(true);
        mockedDeviceInfoTwo.setRam(256);
        mockedDeviceInfoTwo.setResolution(new Pair<>(200, 200));
        mockedDeviceInfoTwo.setDpi(240);

        DeviceInformation mockedDeviceInfoThree = new DeviceInformation();
        mockedDeviceInfoThree.setSerialNumber(THIRD_DEVICE_SERIAL_NUMBER);
        mockedDeviceInfoThree.setOs("4.0.2");
        mockedDeviceInfoThree.setEmulator(false);
        mockedDeviceInfoThree.setRam(512);
        mockedDeviceInfoThree.setResolution(new Pair<>(400, 500));
        mockedDeviceInfoThree.setDpi(80);

        DeviceInformation mockedDeviceInfoFour = new DeviceInformation();
        mockedDeviceInfoFour.setSerialNumber(FOURTH_DEVICE_SERIAL_NUMBER);
        mockedDeviceInfoFour.setOs("4.0.1");
        mockedDeviceInfoFour.setEmulator(true);
        mockedDeviceInfoFour.setRam(512);
        mockedDeviceInfoFour.setResolution(new Pair<>(200, 200));
        mockedDeviceInfoFour.setDpi(180);
        mockedDeviceInfoFour.setCamera(true);

        DeviceInformation mockedDeviceInfoFive = new DeviceInformation();
        mockedDeviceInfoFive.setSerialNumber(FIFTH_DEVICE_SERIAL_NUMBER);
        mockedDeviceInfoFive.setOs("4.3.1");
        mockedDeviceInfoFive.setEmulator(true);
        mockedDeviceInfoFive.setRam(0x7fffffff); // MAX_INT
        mockedDeviceInfoFive.setResolution(new Pair<>(123, 456));
        mockedDeviceInfoFive.setDpi(314);
        mockedDeviceInfoFive.setCamera(false);

        firstDevice = new Device(mockedDeviceInfoOne, FIRST_DEVICE_ID, FIRST_DEVICE_PASSKEY);
        secondDevice = new Device(mockedDeviceInfoTwo, SECOND_DEVICE_ID, SECOND_DEVICE_PASSKEY);
        thirdDevice = new Device(mockedDeviceInfoThree, THIRD_DEVICE_ID, THIRD_DEVICE_PASSKEY);
        fourthDevice = new Device(mockedDeviceInfoFour, FOURTH_DEVICE_ID, FOURTH_DEVICE_PASSKEY);
        fifthDevice = new Device(mockedDeviceInfoFive, FIFTH_DEVICE_ID, FIFTH_DEVICE_PASSKEY);

        deviceList = Arrays.asList(firstDevice, secondDevice, thirdDevice, fourthDevice, fifthDevice);

        for (IDevice device : deviceList) {
            Device deviceModel = (Device) device;
            deviceModel.setAgent(mockedAgent);
            registerMockedDevice(device);
        }
    }

    public static void registerMockedDevice(IDevice device) throws Exception {
        DeviceInformation deviceInformation = device.getInformation();

        when(devicePoolDao.addDevice(any(DeviceInformation.class),
                                     any(String.class),
                                     any(String.class),
                                     any(Long.class))).thenReturn(device);

        poolManager.addDevice(deviceInformation, AGENT_ID);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Class<?> poolManagerClass = PoolManager.class;

        Method deviceIdBuild = poolManagerClass.getDeclaredMethod(BUILD_DEVICE_IDENTIFIER_METHOD_NAME,
                                                                  String.class,
                                                                  String.class);
        deviceIdBuild.setAccessible(true);

        String firstDeviceId = (String) deviceIdBuild.invoke(null, AGENT_ID, FIRST_DEVICE_SERIAL_NUMBER);
        when(devicePoolDao.getDevice(eq(firstDeviceId))).thenReturn(firstDevice);
        doNothing().when(devicePoolDao).remove(firstDeviceId);
        poolManager.removeDevice(firstDeviceId);

        String secondDeviceId = (String) deviceIdBuild.invoke(null, AGENT_ID, SECOND_DEVICE_SERIAL_NUMBER);
        when(devicePoolDao.getDevice(eq(secondDeviceId))).thenReturn(secondDevice);
        doNothing().when(devicePoolDao).remove(secondDeviceId);
        poolManager.removeDevice(secondDeviceId);

        String thirdDeviceId = (String) deviceIdBuild.invoke(null, AGENT_ID, THIRD_DEVICE_SERIAL_NUMBER);
        when(devicePoolDao.getDevice(eq(thirdDeviceId))).thenReturn(thirdDevice);
        doNothing().when(devicePoolDao).remove(thirdDeviceId);
        poolManager.removeDevice(thirdDeviceId);

        String fourthDeviceId = (String) deviceIdBuild.invoke(null, AGENT_ID, FOURTH_DEVICE_SERIAL_NUMBER);
        when(devicePoolDao.getDevice(eq(fourthDeviceId))).thenReturn(fourthDevice);
        doNothing().when(devicePoolDao).remove(fourthDeviceId);
        poolManager.removeDevice(fourthDeviceId);

        String fifthDeviceId = (String) deviceIdBuild.invoke(null, AGENT_ID, FIFTH_DEVICE_SERIAL_NUMBER);
        when(devicePoolDao.getDevice(eq(fifthDeviceId))).thenReturn(fifthDevice);
        doNothing().when(devicePoolDao).remove(fifthDeviceId);
        poolManager.removeDevice(fifthDeviceId);
    }

    @Test(expected = NoDeviceMatchingTheGivenSelectorException.class)
    public void getNotPresentDevice() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().deviceOs(DeviceOs.JELLY_BEAN_MR1_4_2_1)
                                                                           .deviceType(DeviceType.DEVICE_ONLY)
                                                                           .ramCapacity(256);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList();
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);

        poolManager.allocateDevice(deviceSelector);
    }

    @Test
    public void getPresentDeviceFirstTestOne() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().ramCapacity(128);
        DeviceSelector deviceSelector = selectorBuilder.build();

        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(firstDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(firstDevice);

        poolManager.releaseDevice(deviceId);
        assertCorrectDeviceFetched(FIRST_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFirstTestTwo() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.EMULATOR_PREFERRED)
                                                                           .screenHeight(600)
                                                                           .screenWidth(800);
        DeviceSelector deviceSelector = selectorBuilder.build();

        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(firstDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(firstDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FIRST_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFirstTestThree() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().ramCapacity(128);
        DeviceSelector deviceSelector = selectorBuilder.build();

        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(firstDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(firstDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FIRST_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceSecond() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().screenDpi(240);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList(secondDevice);
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(secondDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(secondDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(SECOND_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceThird() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().ramCapacity(512)
                                                                           .deviceType(DeviceType.DEVICE_ONLY);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList(thirdDevice);
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(thirdDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(thirdDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(THIRD_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFourth() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.EMULATOR_ONLY)
                                                                           .screenDpi(180);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList(fourthDevice);
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(fourthDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(fourthDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FOURTH_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceWithCamera() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.EMULATOR_ONLY)
                                                                           .isCameraAvailable(true);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList(fourthDevice);
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(fourthDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(fourthDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FOURTH_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceWithoutCamera() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().isCameraAvailable(false);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList(fifthDevice);
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(deviceSelector);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(fifthDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(fifthDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FIFTH_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test(expected = NoDeviceMatchingTheGivenSelectorException.class)
    public void getMissingDeviceWithCamera() throws Exception {
        DeviceSelectorBuilder selectorBuilder = new DeviceSelectorBuilder().deviceType(DeviceType.DEVICE_ONLY)
                                                                           .screenDpi(144)
                                                                           .isCameraAvailable(true);
        DeviceSelector deviceSelector = selectorBuilder.build();

        List<IDevice> deviceList = Arrays.asList();
        when(devicePoolDao.getDevices(eq(deviceSelector), eq(false))).thenReturn(deviceList);

        poolManager.allocateDevice(deviceSelector);
    }

    private void assertCorrectDeviceFetched(String expectedDeviceSerialNumber,
                                            DeviceAllocationInformation allocatedDeviceInformation) {
        String deviceId = allocatedDeviceInformation.getDeviceId();
        assertEquals("Failed to receive the ID of the correct device.",
                     String.format(DEVICE_ID_FORMAT, expectedDeviceSerialNumber),
                     deviceId);
    }

}
