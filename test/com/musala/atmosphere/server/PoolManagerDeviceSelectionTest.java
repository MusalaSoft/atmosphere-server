package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
import com.musala.atmosphere.server.pool.PoolManager;

public class PoolManagerDeviceSelectionTest {

    private final static int POOL_MANAGER_RMI_PORT = 1234;

    private final static String AGENT_ID = "mockagent";

    private final static String DEVICE1_SN = "mockdevice1";

    private final static String DEVICE2_SN = "mockdevice2";

    private final static String DEVICE3_SN = "mockdevice3";

    private final static String DEVICE4_SN = "mockdevice4";

    private static final String DEVICE5_SN = "mockdevice5";

    private static ServerManager serverManager;

    private static PoolManager poolManager;

    @BeforeClass
    public static void setUp() throws Exception {
        serverManager = new ServerManager(POOL_MANAGER_RMI_PORT);
        poolManager = PoolManager.getInstance();

        IAgentManager mockedAgentManager = mock(IAgentManager.class);
        when(mockedAgentManager.getAgentId()).thenReturn(AGENT_ID);

        Registry mockRegistry = mock(Registry.class);

        DeviceInformation mockedDeviceInfoOne = new DeviceInformation();
        mockedDeviceInfoOne.setSerialNumber(DEVICE1_SN);
        mockedDeviceInfoOne.setOs("4.2.1");
        mockedDeviceInfoOne.setEmulator(true);
        mockedDeviceInfoOne.setRam(128);
        mockedDeviceInfoOne.setResolution(new Pair<>(600, 800));
        mockedDeviceInfoOne.setDpi(120);

        DeviceInformation mockedDeviceInfoTwo = new DeviceInformation();
        mockedDeviceInfoTwo.setSerialNumber(DEVICE2_SN);
        mockedDeviceInfoTwo.setOs("4.1.1");
        mockedDeviceInfoTwo.setEmulator(true);
        mockedDeviceInfoTwo.setRam(256);
        mockedDeviceInfoTwo.setResolution(new Pair<>(200, 200));
        mockedDeviceInfoTwo.setDpi(240);

        DeviceInformation mockedDeviceInfoThree = new DeviceInformation();
        mockedDeviceInfoThree.setSerialNumber(DEVICE3_SN);
        mockedDeviceInfoThree.setOs("4.0.2");
        mockedDeviceInfoThree.setEmulator(false);
        mockedDeviceInfoThree.setRam(512);
        mockedDeviceInfoThree.setResolution(new Pair<>(400, 500));
        mockedDeviceInfoThree.setDpi(80);

        DeviceInformation mockedDeviceInfoFour = new DeviceInformation();
        mockedDeviceInfoFour.setSerialNumber(DEVICE4_SN);
        mockedDeviceInfoFour.setOs("4.0.1");
        mockedDeviceInfoFour.setEmulator(true);
        mockedDeviceInfoFour.setRam(512);
        mockedDeviceInfoFour.setResolution(new Pair<>(200, 200));
        mockedDeviceInfoFour.setDpi(180);
        mockedDeviceInfoFour.setCamera(true);

        DeviceInformation mockedDeviceInfoFive = new DeviceInformation();
        mockedDeviceInfoFive.setSerialNumber(DEVICE5_SN);
        mockedDeviceInfoFive.setOs("4.3.1");
        mockedDeviceInfoFive.setEmulator(true);
        mockedDeviceInfoFive.setRam(0x7fffffff); // MAX_INT
        mockedDeviceInfoFive.setResolution(new Pair<>(123, 456));
        mockedDeviceInfoFive.setDpi(314);
        mockedDeviceInfoFive.setCamera(false);

        List<DeviceInformation> deviceInfos = Arrays.asList(mockedDeviceInfoOne,
                                                            mockedDeviceInfoTwo,
                                                            mockedDeviceInfoThree,
                                                            mockedDeviceInfoFour,
                                                            mockedDeviceInfoFive);
        for (DeviceInformation aDeviceInfo : deviceInfos) {
            registerMockedDevice(aDeviceInfo, AGENT_ID, mockedAgentManager, mockRegistry);
        }
    }

    public static void registerMockedDevice(DeviceInformation mockDevInfo,
                                            String agentId,
                                            IAgentManager mockedAgentManager,
                                            Registry mockedRegistry) throws Exception {
        IWrapDevice mockedDevice = mock(IWrapDevice.class);
        String deviceId = mockDevInfo.getSerialNumber();
        when(mockedRegistry.lookup(deviceId)).thenReturn(mockedDevice);
        when(mockedDevice.route(eq(RoutingAction.GET_DEVICE_INFORMATION))).thenReturn(mockDevInfo);
        poolManager.addDevice(deviceId, mockedRegistry, mockedAgentManager, POOL_MANAGER_RMI_PORT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        poolManager.removeDevice(DEVICE1_SN, AGENT_ID);
        poolManager.removeDevice(DEVICE2_SN, AGENT_ID);
        poolManager.removeDevice(DEVICE3_SN, AGENT_ID);
        poolManager.removeDevice(DEVICE4_SN, AGENT_ID);
        poolManager.removeDevice(DEVICE5_SN, AGENT_ID);
        serverManager.close();
    }

    @Test(expected = NoAvailableDeviceFoundException.class)
    public void getNotPresentDevice() throws RemoteException {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setOs(DeviceOs.JELLY_BEAN_MR1_4_2_1);
        parameters.setDeviceType(DeviceType.DEVICE_ONLY);
        parameters.setRam(256);

        poolManager.allocateDevice(parameters);
    }

    @Test
    public void getPresentDeviceFirstTestOne() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setRam(128);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE1_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFirstTestTwo() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.EMULATOR_PREFERRED);
        parameters.setResolutionHeight(600);
        parameters.setResolutionWidth(800);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE1_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFirstTestThree() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setRam(128);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE1_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceSecond() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDpi(240);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE2_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceThird() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.DEVICE_ONLY);
        parameters.setRam(512);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE3_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceFourth() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.EMULATOR_ONLY);
        parameters.setDpi(180);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE4_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceWithCamera() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.EMULATOR_ONLY);
        parameters.setCameraPresent(true);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE4_SN, deviceDescriptor);
    }

    @Test
    public void getPresentDeviceWithoutCamera() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setCameraPresent(false);

        DeviceAllocationInformation deviceDescriptor = poolManager.allocateDevice(parameters);
        poolManager.releaseDevice(deviceDescriptor);
        assertCorrectDeviceFetched(DEVICE5_SN, deviceDescriptor);
    }

    @Test(expected = NoAvailableDeviceFoundException.class)
    public void getMissingDeviceWithCamera() throws Exception {
        DeviceParameters parameters = new DeviceParameters();
        parameters.setDeviceType(DeviceType.DEVICE_ONLY);
        parameters.setDpi(0xdeadbeef);
        parameters.setCameraPresent(true);

        poolManager.allocateDevice(parameters);
    }

    private void assertCorrectDeviceFetched(String expectedDeviceSN,
                                            DeviceAllocationInformation allocatedDeviceInformation) {
        String deviceRmiID = allocatedDeviceInformation.getProxyRmiId();
        assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + expectedDeviceSN, deviceRmiID);
    }

}
