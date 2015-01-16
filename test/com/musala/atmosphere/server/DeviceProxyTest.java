package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsArgAt;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.data.dao.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;

@RunWith(MockitoJUnitRunner.class)
public class DeviceProxyTest {
    private IWrapDevice innerDeviceWrapperMock;

    private static Field deviceProxyMonitorField;

    private static ClientRequestMonitor mockMonitor;

    private DeviceProxy deviceProxy;

    private static long proxyPasskey = 1;

    private static final String DEVICE_ID = "12345_123";

    private final static PhoneNumber PHONE_NUMBER = new PhoneNumber("123");

    private static DevicePoolDao devicePoolDao;

    private static IDevice device;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // mock the ClientRequestMonitor singleton
        mockMonitor = mock(ClientRequestMonitor.class);
        Mockito.doNothing().when(mockMonitor).restartTimerForDevice(DEVICE_ID);

        // set proxy's ClientRequestMonitor field with the mocked monitor
        deviceProxyMonitorField = DeviceProxy.class.getDeclaredField("timeoutMonitor");
        deviceProxyMonitorField.setAccessible(true);

        device = new Device(new DeviceInformation(), DEVICE_ID, proxyPasskey);

        devicePoolDao = mock(DevicePoolDao.class);
        when(devicePoolDao.getDevice(eq(DEVICE_ID))).thenReturn(device);

        Field poolDao = DataSourceProvider.class.getDeclaredField("devicePoolDao");
        poolDao.setAccessible(true);
        poolDao.set(null, devicePoolDao);
    }

    @Before
    public void setUp() throws Exception {
        // instantiate the mocked device
        innerDeviceWrapperMock = mock(IWrapDevice.class);
        deviceProxy = new DeviceProxy(innerDeviceWrapperMock, DEVICE_ID);

        when(innerDeviceWrapperMock.route(any(RoutingAction.class))).thenReturn(RoutingAction.CALL_ACCEPT);
        deviceProxyMonitorField.set(deviceProxy, mockMonitor);
    }

    @Test
    public void testExecuteShellCommand() throws Exception {
        when(innerDeviceWrapperMock.route(eq(RoutingAction.EXECUTE_SHELL_COMMAND), anyString())).then(returnsArgAt(1));

        String inputCommand = "abc";
        String response = (String) deviceProxy.route(proxyPasskey, RoutingAction.EXECUTE_SHELL_COMMAND, inputCommand);

        assertEquals("Input did not match expected response.", inputCommand, response);
        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.EXECUTE_SHELL_COMMAND, inputCommand);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = CommandFailedException.class)
    public void testExecuteShellCommandFailed() throws Exception {
        when(innerDeviceWrapperMock.route(eq(RoutingAction.EXECUTE_SHELL_COMMAND), anyString())).thenThrow(new CommandFailedException());

        String inputCommand = "abc";
        deviceProxy.route(proxyPasskey, RoutingAction.EXECUTE_SHELL_COMMAND, inputCommand);
    }

    @Test
    public void testGetFreeRam() throws Exception {
        long ramAmount = 123;
        when(innerDeviceWrapperMock.route(RoutingAction.GET_FREE_RAM)).thenReturn(ramAmount);

        long response = (long) deviceProxy.route(proxyPasskey, RoutingAction.GET_FREE_RAM);

        assertEquals("Mocked response and actual method response should match.", ramAmount, response);
        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.GET_FREE_RAM);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testGetFreeRamFailed() throws Exception {
        when(innerDeviceWrapperMock.route(RoutingAction.GET_FREE_RAM)).thenThrow(new RemoteException());

        deviceProxy.route(proxyPasskey, RoutingAction.GET_FREE_RAM);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteSequenceOfShellCommands() throws Exception {
        when(innerDeviceWrapperMock.route(eq(RoutingAction.EXECUTE_SHELL_COMMAND_SEQUENCE), (List<String>) any())).then(returnsArgAt(1));

        List<String> inputCommand = Arrays.asList(new String[] {"abc", "cde"});
        List<String> response = (List<String>) deviceProxy.route(proxyPasskey,
                                                                 RoutingAction.EXECUTE_SHELL_COMMAND_SEQUENCE,
                                                                 inputCommand);

        assertEquals("Input and response should match.", inputCommand, response);
        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.EXECUTE_SHELL_COMMAND_SEQUENCE, inputCommand);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void testExecuteSequenceOfShellCommandsFailed() throws Exception {
        when(innerDeviceWrapperMock.route(RoutingAction.EXECUTE_SHELL_COMMAND_SEQUENCE, (List<String>) any())).thenThrow(new RemoteException());

        List<String> inputCommand = Arrays.asList(new String[] {"abc", "cde"});
        deviceProxy.route(proxyPasskey, RoutingAction.EXECUTE_SHELL_COMMAND_SEQUENCE, inputCommand);
    }

    @Test
    public void testInitApkInstall() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.APK_INIT_INSTALL);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.APK_INIT_INSTALL);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testInitApkInstallFailed() throws Exception {
        Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).route(RoutingAction.APK_INIT_INSTALL);

        deviceProxy.route(proxyPasskey, RoutingAction.APK_INIT_INSTALL);
    }

    @Test
    public void testAppendToApk() throws Exception {
        byte[] testBytes = new byte[] {0, 1, 2, 3};
        deviceProxy.route(proxyPasskey, RoutingAction.APK_APPEND_DATA, testBytes, 4);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.APK_APPEND_DATA, testBytes, testBytes.length);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = CommandFailedException.class)
    public void testAppendToApkFailed() throws Exception {
        byte[] testBytes = new byte[] {0, 1, 2, 3};
        Mockito.doThrow(new CommandFailedException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.APK_APPEND_DATA), any(byte[].class), anyInt());

        deviceProxy.route(proxyPasskey, RoutingAction.APK_APPEND_DATA, testBytes, 4);
    }

    @Test
    public void testBuildAndInstallApk() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.APK_BUILD_AND_INSTALL);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.APK_BUILD_AND_INSTALL);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = CommandFailedException.class)
    public void testBuildAndInstallApkFailed() throws Exception {
        Mockito.doThrow(new CommandFailedException())
               .when(innerDeviceWrapperMock)
               .route(RoutingAction.APK_BUILD_AND_INSTALL);

        deviceProxy.route(proxyPasskey, RoutingAction.APK_BUILD_AND_INSTALL);
    }

    @Test
    public void testGetUiXml() throws Exception {
        String testDataResponse = "testresponse";
        when(innerDeviceWrapperMock.route(RoutingAction.GET_UI_XML_DUMP)).thenReturn(testDataResponse);

        String response = (String) deviceProxy.route(proxyPasskey, RoutingAction.GET_UI_XML_DUMP);

        assertEquals("Mocked response and actual method response should match.", testDataResponse, response);
        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.GET_UI_XML_DUMP);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = CommandFailedException.class)
    public void testGetUiXmlFailed() throws Exception {
        when(innerDeviceWrapperMock.route(RoutingAction.GET_UI_XML_DUMP)).thenThrow(new CommandFailedException());

        deviceProxy.route(proxyPasskey, RoutingAction.GET_UI_XML_DUMP);
    }

    @Test
    public void testGetScreenShot() throws Exception {
        byte[] testDataResponse = new byte[] {0, 1, 2, 3};
        when(innerDeviceWrapperMock.route(RoutingAction.GET_SCREENSHOT)).thenReturn(testDataResponse);

        byte[] response = (byte[]) deviceProxy.route(proxyPasskey, RoutingAction.GET_SCREENSHOT);

        // converting arrays to lists, as assertEquals(array, array) is deprecated.
        assertEquals("Mocked response and actual method response should match.",
                     Arrays.asList(testDataResponse),
                     Arrays.asList(response));
        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.GET_SCREENSHOT);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testGetScreenShotFailed() throws Exception {
        when(innerDeviceWrapperMock.route(RoutingAction.GET_SCREENSHOT)).thenThrow(new RemoteException());

        deviceProxy.route(proxyPasskey, RoutingAction.GET_SCREENSHOT);
    }

    @Test
    public void testSetNetworkSpeed() throws Exception {
        Pair<Integer, Integer> testInput = new Pair<Integer, Integer>(0, 0);

        deviceProxy.route(proxyPasskey, RoutingAction.SET_NETWORK_SPEED, testInput);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.SET_NETWORK_SPEED, testInput);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void testSetNetworkSpeedFailed() throws Exception {
        Pair<Integer, Integer> testInput = new Pair<Integer, Integer>(0, 0);
        Mockito.doThrow(new RemoteException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.SET_NETWORK_SPEED), any());

        deviceProxy.route(proxyPasskey, RoutingAction.SET_NETWORK_SPEED, testInput);
    }

    @Test
    public void testSetPowerProperties() throws Exception {
        PowerProperties testInput = new PowerProperties();
        deviceProxy.route(proxyPasskey, RoutingAction.SET_POWER_PROPERTIES, testInput);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.SET_POWER_PROPERTIES, testInput);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testSetPowerPropertiesFailed() throws Exception {
        PowerProperties testInput = new PowerProperties();
        Mockito.doThrow(new RemoteException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.SET_POWER_PROPERTIES), (PowerProperties) any());
        deviceProxy.route(proxyPasskey, RoutingAction.SET_POWER_PROPERTIES, testInput);
    }

    @Test
    public void testGetBatteryState() throws Exception {
        PowerProperties testDataResponse = new PowerProperties();
        when(innerDeviceWrapperMock.route(RoutingAction.GET_POWER_PROPERTIES)).thenReturn(testDataResponse);

        PowerProperties response = (PowerProperties) deviceProxy.route(proxyPasskey, RoutingAction.GET_POWER_PROPERTIES);
        assertEquals("Expected response and method response should match.", testDataResponse, response);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.GET_POWER_PROPERTIES);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPowerPropertiesFailed() throws Exception {
        when(innerDeviceWrapperMock.route(RoutingAction.GET_POWER_PROPERTIES)).thenThrow(new RemoteException());

        deviceProxy.route(proxyPasskey, RoutingAction.GET_POWER_PROPERTIES);
    }

    @Test
    public void testReceiveSms() throws Exception {
        SmsMessage smsMessage = new SmsMessage(PHONE_NUMBER, "");

        deviceProxy.route(proxyPasskey, RoutingAction.SMS_RECEIVE, smsMessage);
        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.SMS_RECEIVE, smsMessage);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testReceiveSmsFailed() throws Exception {
        SmsMessage smsMessage = new SmsMessage(PHONE_NUMBER, "");

        Mockito.doThrow(new RemoteException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.SMS_RECEIVE), any(SmsMessage.class));
        deviceProxy.route(proxyPasskey, RoutingAction.SMS_RECEIVE, smsMessage);
    }

    @Test
    public void testReceiveCall() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.CALL_RECEIVE, PHONE_NUMBER);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.CALL_RECEIVE, PHONE_NUMBER);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testReceiveCallFailed() throws Exception {
        Mockito.doThrow(new RemoteException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.CALL_RECEIVE), any(PhoneNumber.class));
        deviceProxy.route(proxyPasskey, RoutingAction.CALL_RECEIVE, PHONE_NUMBER);
    }

    @Test
    public void testAcceptCall() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.CALL_ACCEPT, PHONE_NUMBER);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.CALL_ACCEPT, PHONE_NUMBER);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testAcceptCallFailed() throws Exception {
        Mockito.doThrow(new RuntimeException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.CALL_ACCEPT), any(PhoneNumber.class));
        deviceProxy.route(proxyPasskey, RoutingAction.CALL_ACCEPT, PHONE_NUMBER);
    }

    @Test
    public void testHoldCall() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.CALL_HOLD, PHONE_NUMBER);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.CALL_HOLD, PHONE_NUMBER);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testHoldCallFailed() throws Exception {
        Mockito.doThrow(new RemoteException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.CALL_HOLD), any(PhoneNumber.class));

        deviceProxy.route(proxyPasskey, RoutingAction.CALL_HOLD, PHONE_NUMBER);

    }

    @Test
    public void testCancelCall() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.CALL_CANCEL, PHONE_NUMBER);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.CALL_CANCEL, PHONE_NUMBER);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testCancelCallFailed() throws Exception {
        Mockito.doThrow(new RuntimeException())
               .when(innerDeviceWrapperMock)
               .route(eq(RoutingAction.CALL_CANCEL), any(PhoneNumber.class));

        deviceProxy.route(proxyPasskey, RoutingAction.CALL_CANCEL, PHONE_NUMBER);
    }

    @Test
    public void testGetTelephonyInformation() throws Exception {
        deviceProxy.route(proxyPasskey, RoutingAction.GET_TELEPHONY_INFO);

        verify(innerDeviceWrapperMock, times(1)).route(RoutingAction.GET_TELEPHONY_INFO);
        verifyNoMoreInteractions(innerDeviceWrapperMock);
    }

    @Test(expected = RuntimeException.class)
    public void testGetTelephonyInformationFailed() throws Exception {
        Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).route(eq(RoutingAction.GET_TELEPHONY_INFO));
        deviceProxy.route(proxyPasskey, RoutingAction.GET_TELEPHONY_INFO);
    }
}
