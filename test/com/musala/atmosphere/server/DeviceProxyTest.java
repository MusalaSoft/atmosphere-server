package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.IWrapDevice;

public class DeviceProxyTest
{
	private IWrapDevice innerDeviceWrapperMock;

	private DeviceProxy deviceProxy;

	private long proxyPasskey;

	@Before
	public void setUpClass() throws Exception
	{
		innerDeviceWrapperMock = mock(IWrapDevice.class);
		deviceProxy = new DeviceProxy(innerDeviceWrapperMock);
		proxyPasskey = PasskeyAuthority.getInstance().getPasskey(deviceProxy);
	}

	@Test
	public void testExecuteShellCommand() throws Exception
	{
		when(innerDeviceWrapperMock.executeShellCommand(anyString())).then(returnsFirstArg());

		String inputCommand = "abc";
		String response = deviceProxy.executeShellCommand(inputCommand, proxyPasskey);

		assertEquals("Input did not match expected response.", inputCommand, response);
		verify(innerDeviceWrapperMock, times(1)).executeShellCommand(inputCommand);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = CommandFailedException.class)
	public void testExecuteShellCommandFailed() throws Exception
	{
		when(innerDeviceWrapperMock.executeShellCommand(anyString())).thenThrow(new CommandFailedException());

		String inputCommand = "abc";
		deviceProxy.executeShellCommand(inputCommand, proxyPasskey);
	}

	@Test
	public void testGetFreeRam() throws Exception
	{
		long ramAmount = 123;
		when(innerDeviceWrapperMock.getFreeRAM()).thenReturn(ramAmount);

		long response = deviceProxy.getFreeRam(proxyPasskey);

		assertEquals("Mocked response and actual method response should match.", ramAmount, response);
		verify(innerDeviceWrapperMock, times(1)).getFreeRAM();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetFreeRamFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getFreeRAM()).thenThrow(new RemoteException());

		deviceProxy.getFreeRam(proxyPasskey);
	}

	@Test
	public void testExecuteSequenceOfShellCommands() throws Exception
	{
		when(innerDeviceWrapperMock.executeSequenceOfShellCommands((List<String>) any())).then(returnsFirstArg());

		List<String> inputCommand = Arrays.asList(new String[] {"abc", "cde"});
		List<String> response = deviceProxy.executeSequenceOfShellCommands(inputCommand, proxyPasskey);

		assertEquals("Input and response should match.", inputCommand, response);
		verify(innerDeviceWrapperMock, times(1)).executeSequenceOfShellCommands(inputCommand);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testExecuteSequenceOfShellCommandsFailed() throws Exception
	{
		when(innerDeviceWrapperMock.executeSequenceOfShellCommands((List<String>) any())).thenThrow(new RemoteException());

		List<String> inputCommand = Arrays.asList(new String[] {"abc", "cde"});
		deviceProxy.executeSequenceOfShellCommands(inputCommand, proxyPasskey);
	}

	@Test
	public void testInitApkInstall() throws Exception
	{
		deviceProxy.initApkInstall(proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).initAPKInstall();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testInitApkInstallFailed() throws Exception
	{
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).initAPKInstall();

		deviceProxy.initApkInstall(proxyPasskey);
	}

	@Test
	public void testAppendToApk() throws Exception
	{
		byte[] testBytes = new byte[] {0, 1, 2, 3};
		deviceProxy.appendToApk(testBytes, proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).appendToAPK(testBytes);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = IOException.class)
	public void testAppendToApkFailed() throws Exception
	{
		byte[] testBytes = new byte[] {0, 1, 2, 3};
		Mockito.doThrow(new IOException()).when(innerDeviceWrapperMock).appendToAPK((byte[]) any());

		deviceProxy.appendToApk(testBytes, proxyPasskey);
	}

	@Test
	public void testBuildAndInstallApk() throws Exception
	{
		deviceProxy.buildAndInstallApk(proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).buildAndInstallAPK();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = IOException.class)
	public void testBuildAndInstallApkFailed() throws Exception
	{
		Mockito.doThrow(new IOException()).when(innerDeviceWrapperMock).buildAndInstallAPK();

		deviceProxy.buildAndInstallApk(proxyPasskey);
	}

	@Test
	public void testDiscardApk() throws Exception
	{
		deviceProxy.discardApk(proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).discardAPK();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test
	public void testGetUiXml() throws Exception
	{
		String testDataResponse = "testresponse";
		when(innerDeviceWrapperMock.getUiXml()).thenReturn(testDataResponse);

		String response = deviceProxy.getUiXml(proxyPasskey);

		assertEquals("Mocked response and actual method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getUiXml();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = CommandFailedException.class)
	public void testGetUiXmlFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getUiXml()).thenThrow(new CommandFailedException());

		deviceProxy.getUiXml(proxyPasskey);
	}

	@Test
	public void testGetScreenShot() throws Exception
	{
		byte[] testDataResponse = new byte[] {0, 1, 2, 3};
		when(innerDeviceWrapperMock.getScreenshot()).thenReturn(testDataResponse);

		byte[] response = deviceProxy.getScreenshot(proxyPasskey);

		// converting arrays to lists, as assertEquals(array, array) is deprecated.
		assertEquals(	"Mocked response and actual method response should match.",
						Arrays.asList(testDataResponse),
						Arrays.asList(response));
		verify(innerDeviceWrapperMock, times(1)).getScreenshot();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetScreenShotFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getScreenshot()).thenThrow(new RemoteException());

		deviceProxy.getScreenshot(proxyPasskey);
	}

	@Test
	public void testSetNetworkSpeed() throws Exception
	{
		Pair<Integer, Integer> testInput = new Pair<Integer, Integer>(0, 0);

		deviceProxy.setNetworkSpeed(testInput, proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).setNetworkSpeed(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetNetworkSpeedFailed() throws Exception
	{
		Pair<Integer, Integer> testInput = new Pair<Integer, Integer>(0, 0);
		Mockito.doThrow(new RemoteException())
				.when(innerDeviceWrapperMock)
				.setNetworkSpeed((Pair<Integer, Integer>) any());

		deviceProxy.setNetworkSpeed(testInput, proxyPasskey);
	}

	@Test
	public void testGetNetworkSpeed() throws Exception
	{
		Pair<Integer, Integer> testDataResponse = new Pair<Integer, Integer>(0, 0);
		when(innerDeviceWrapperMock.getNetworkSpeed()).thenReturn(testDataResponse);

		Pair<Integer, Integer> response = deviceProxy.getNetworkSpeed(proxyPasskey);

		assertEquals("Mocked response and actual method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getNetworkSpeed();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetNetworkSpeedFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getNetworkSpeed()).thenThrow(new RemoteException());

		deviceProxy.getNetworkSpeed(proxyPasskey);
	}

	@Test
	public void testSetNetworkLatency() throws Exception
	{
		int testInput = 123;

		deviceProxy.setNetworkLatency(testInput, proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).setNetworkLatency(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetNetworkLatencyFailed() throws Exception
	{
		int testInput = 123;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setNetworkLatency(anyInt());

		deviceProxy.setNetworkLatency(testInput, proxyPasskey);
	}

	@Test
	public void testGetNetworkLatency() throws Exception
	{
		int testDataResponse = 123;
		when(innerDeviceWrapperMock.getNetworkLatency()).thenReturn(testDataResponse);

		int response = deviceProxy.getNetworkLatency(proxyPasskey);

		assertEquals("Input and response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getNetworkLatency();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetNetworkLatencyFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getNetworkLatency()).thenThrow(new RemoteException());

		deviceProxy.getNetworkLatency(proxyPasskey);
	}

	@Test
	public void testSetBatteryLevel() throws Exception
	{
		int testInput = 123;

		deviceProxy.setBatteryLevel(testInput, proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).setBatteryLevel(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetBatteryLevelFailed() throws Exception
	{
		int testInput = 123;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setBatteryLevel(anyInt());

		deviceProxy.setBatteryLevel(testInput, proxyPasskey);
	}

	@Test
	public void testGetBatteryLevel() throws Exception
	{
		int testDataResponse = 123;
		when(innerDeviceWrapperMock.getBatteryLevel()).thenReturn(testDataResponse);

		int response = deviceProxy.getBatteryLevel(proxyPasskey);

		assertEquals("Expected response and method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getBatteryLevel();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetBatteryLevelFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getBatteryLevel()).thenThrow(new RemoteException());

		deviceProxy.getBatteryLevel(proxyPasskey);
	}

	@Test
	public void testSetBatteryState() throws Exception
	{
		BatteryState testInput = BatteryState.CHARGING;

		deviceProxy.setBatteryState(testInput, proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).setBatteryState(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetBatteryStateFailed() throws Exception
	{
		BatteryState testInput = BatteryState.CHARGING;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setBatteryState((BatteryState) any());

		deviceProxy.setBatteryState(testInput, proxyPasskey);
	}

	@Test
	public void testGetBatteryState() throws Exception
	{
		BatteryState testDataResponse = BatteryState.CHARGING;
		when(innerDeviceWrapperMock.getBatteryState()).thenReturn(testDataResponse);

		BatteryState response = deviceProxy.getBatteryState(proxyPasskey);

		assertEquals("Expected response and method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getBatteryState();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetBatteryStateFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getBatteryState()).thenThrow(new RemoteException());

		deviceProxy.getBatteryState(proxyPasskey);
	}

	@Test
	public void testSetPowerState() throws Exception
	{
		boolean testInput = false;

		deviceProxy.setPowerState(testInput, proxyPasskey);

		verify(innerDeviceWrapperMock, times(1)).setPowerState(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetPowerStateFailed() throws Exception
	{
		boolean testInput = false;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setPowerState(anyBoolean());

		deviceProxy.setPowerState(testInput, proxyPasskey);
	}

	@Test
	public void testGetPowerState() throws Exception
	{
		boolean testDataResponse = false;
		when(innerDeviceWrapperMock.getPowerState()).thenReturn(testDataResponse);

		boolean response = deviceProxy.getPowerState(proxyPasskey);

		assertEquals("Expected response and method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getPowerState();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetPowerStateFailed() throws Exception
	{
		when(innerDeviceWrapperMock.getPowerState()).thenThrow(new RemoteException());

		deviceProxy.getPowerState(proxyPasskey);
	}

}
