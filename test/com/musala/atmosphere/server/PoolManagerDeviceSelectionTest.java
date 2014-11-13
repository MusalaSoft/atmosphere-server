package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.rmi.registry.Registry;
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
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.dao.nativeobject.Device;
import com.musala.atmosphere.server.dao.nativeobject.DevicePoolDao;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.pool.PoolManager;

@RunWith(MockitoJUnitRunner.class)
public class PoolManagerDeviceSelectionTest {
    private final static String BUILD_DEVICE_IDENTIFIER_METHOD_NAME = "buildDeviceIdentifier";

    private final static String AGENT_ID = "mockagent";

    private final static String FIRST_DEVICE_SERIAL_NUMBER = "mockdevice1";

    private final static String SECOND_DEVICE_SERIAL_NUMBER = "mockdevice2";

    private final static String THIRD_DEVICE_SERIAL_NUMBER = "mockdevice3";

    private final static String FOURTH_DEVICE_SERIAL_NUMBER = "mockdevice4";

    private static final String FIFTH_DEVICE_SERIAL_NUMBER = "mockdevice5";

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

    @InjectMocks
    private static PoolManager poolManager = PoolManager.getInstance();

    @Mock
    private static DevicePoolDao devicePoolDao;

    private static Registry mockedRegistry;

    private static IAgentManager mockedAgentManager;

    @BeforeClass
    public static void setUp() throws Exception {
        mockedAgentManager = mock(IAgentManager.class);
        when(mockedAgentManager.getAgentId()).thenReturn(AGENT_ID);

        devicePoolDao = mock(DevicePoolDao.class);

        mockedRegistry = mock(Registry.class);
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

        firstDevice = new Device(mockedDeviceInfoOne, FIRST_DEVICE_SERIAL_NUMBER, AGENT_ID, FIRST_DEVICE_PASSKEY);
        secondDevice = new Device(mockedDeviceInfoTwo, SECOND_DEVICE_SERIAL_NUMBER, AGENT_ID, SECOND_DEVICE_PASSKEY);
        thirdDevice = new Device(mockedDeviceInfoThree, THIRD_DEVICE_SERIAL_NUMBER, AGENT_ID, THIRD_DEVICE_PASSKEY);
        fourthDevice = new Device(mockedDeviceInfoFour, FOURTH_DEVICE_SERIAL_NUMBER, AGENT_ID, FOURTH_DEVICE_PASSKEY);
        fifthDevice = new Device(mockedDeviceInfoFive, FIFTH_DEVICE_SERIAL_NUMBER, AGENT_ID, FIFTH_DEVICE_PASSKEY);

        deviceList = Arrays.asList(firstDevice, secondDevice, thirdDevice, fourthDevice, fifthDevice);

        for (IDevice device : deviceList) {
            registerMockedDevice(device);
        }
    }

    public static void registerMockedDevice(IDevice device) throws Exception {
        IWrapDevice mockedDevice = mock(IWrapDevice.class);
        DeviceInformation deviceInformation = device.getInformation();

        String deviceId = deviceInformation.getSerialNumber();

        when(mockedRegistry.lookup(eq(deviceId))).thenReturn(mockedDevice);
        when(mockedDevice.route(eq(RoutingAction.GET_DEVICE_INFORMATION))).thenReturn(deviceInformation);

        when(devicePoolDao.addDevice(any(DeviceInformation.class),
                                     any(String.class),
                                     any(String.class),
                                     any(Long.class))).thenReturn(device);

        poolManager.addDevice(deviceId, mockedRegistry, AGENT_ID);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Class<?> packageManagerClass = PoolManager.class;

        Method deviceIdBuild = packageManagerClass.getDeclaredMethod(BUILD_DEVICE_IDENTIFIER_METHOD_NAME,
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

    @Test(expected = NoAvailableDeviceFoundException.class)
    public void getNotPresentDevice() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setOs(DeviceOs.JELLY_BEAN_MR1_4_2_1);
        parameters.setDeviceType(DeviceType.DEVICE_ONLY);
        parameters.setRam(256);

        List<IDevice> deviceList = Arrays.asList();
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);

        poolManager.allocateDevice(parameters);
    }

    @Test
    public void getPresentDeviceFirstTestOne() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setRam(128);

        when(devicePoolDao.getDevices(parameters)).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(firstDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(firstDevice);

        poolManager.releaseDevice(deviceId);
        assertCorrectDeviceFetched(FIRST_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFirstTestTwo() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.EMULATOR_PREFERRED);
        parameters.setResolutionHeight(600);
        parameters.setResolutionWidth(800);

        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(firstDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(firstDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FIRST_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFirstTestThree() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setRam(128);

        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(firstDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(firstDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FIRST_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceSecond() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDpi(240);

        List<IDevice> deviceList = Arrays.asList(secondDevice);
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(secondDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(secondDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(SECOND_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceThird() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.DEVICE_ONLY);
        parameters.setRam(512);

        List<IDevice> deviceList = Arrays.asList(thirdDevice);
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(thirdDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(thirdDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(THIRD_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFourth() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.EMULATOR_ONLY);
        parameters.setDpi(180);

        List<IDevice> deviceList = Arrays.asList(fourthDevice);
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(fourthDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(fourthDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FOURTH_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceWithCamera() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.EMULATOR_ONLY);
        parameters.setCameraPresent(true);

        List<IDevice> deviceList = Arrays.asList(fourthDevice);
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(fourthDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(fourthDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FOURTH_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceWithoutCamera() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setCameraPresent(false);

        List<IDevice> deviceList = Arrays.asList(fifthDevice);
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);
        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        String deviceId = deviceDescriptor.getDeviceId();

        doNothing().when(devicePoolDao).update(fifthDevice);
        when(devicePoolDao.getDevice(eq(deviceId))).thenReturn(fifthDevice);

        poolManager.releaseDevice(deviceId);

        assertCorrectDeviceFetched(FIFTH_DEVICE_SERIAL_NUMBER, deviceDescriptor);
    }

    @Test(expected = NoAvailableDeviceFoundException.class)
    public void getMissingDeviceWithCamera() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.DEVICE_ONLY);
        parameters.setDpi(0xdeadbeef);
        parameters.setCameraPresent(true);

        List<IDevice> deviceList = Arrays.asList();
        when(devicePoolDao.getDevices(eq(parameters))).thenReturn(deviceList);

        poolManager.allocateDevice(parameters);
    }

    private void assertCorrectDeviceFetched(String expectedDeviceSerialNumber,
                                            DeviceAllocationInformation allocatedDeviceInformation) {
        String deviceRmiID = allocatedDeviceInformation.getProxyRmiId();
        assertEquals("Failed to receive RMI ID of the correct device.",
                     AGENT_ID + "_" + expectedDeviceSerialNumber,
                     deviceRmiID);
    }

}
