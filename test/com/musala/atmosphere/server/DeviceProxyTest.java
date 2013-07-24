package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
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

	@Before
	public void setUpClass() throws Exception
	{
		innerDeviceWrapperMock = mock(IWrapDevice.class);
		deviceProxy = new DeviceProxy(innerDeviceWrapperMock);
	}

	@Test
	public void testExecuteShellCommand() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.executeShellCommand(anyString())).then(returnsFirstArg());

		String inputCommand = "abc";
		String response = deviceProxy.executeShellCommand(inputCommand);

		assertEquals("Input did not match expected response.", inputCommand, response);
		verify(innerDeviceWrapperMock, times(1)).executeShellCommand(inputCommand);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = CommandFailedException.class)
	public void testExecuteShellCommandFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.executeShellCommand(anyString())).thenThrow(new CommandFailedException());

		String inputCommand = "abc";
		deviceProxy.executeShellCommand(inputCommand);
	}

	@Test
	public void testGetFreeRam() throws RemoteException, CommandFailedException
	{
		long ramAmount = 123;
		when(innerDeviceWrapperMock.getFreeRAM()).thenReturn(ramAmount);

		long response = deviceProxy.getFreeRam();

		assertEquals("Mocked response and actual method response should match.", ramAmount, response);
		verify(innerDeviceWrapperMock, times(1)).getFreeRAM();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetFreeRamFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.getFreeRAM()).thenThrow(new RemoteException());

		deviceProxy.getFreeRam();
	}

	@Test
	public void testExecuteSequenceOfShellCommands() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.executeSequenceOfShellCommands((List<String>) any())).then(returnsFirstArg());

		List<String> inputCommand = Arrays.asList(new String[] {"abc", "cde"});
		List<String> response = deviceProxy.executeSequenceOfShellCommands(inputCommand);

		assertEquals("Input and response should match.", inputCommand, response);
		verify(innerDeviceWrapperMock, times(1)).executeSequenceOfShellCommands(inputCommand);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testExecuteSequenceOfShellCommandsFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.executeSequenceOfShellCommands((List<String>) any())).thenThrow(new RemoteException());

		List<String> inputCommand = Arrays.asList(new String[] {"abc", "cde"});
		deviceProxy.executeSequenceOfShellCommands(inputCommand);
	}

	@Test
	public void testInitApkInstall() throws RemoteException, IOException
	{
		deviceProxy.initApkInstall();

		verify(innerDeviceWrapperMock, times(1)).initAPKInstall();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testInitApkInstallFailed() throws RemoteException, IOException
	{
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).initAPKInstall();

		deviceProxy.initApkInstall();
	}

	@Test
	public void testAppendToApk() throws RemoteException, IOException
	{
		byte[] testBytes = new byte[] {0, 1, 2, 3};
		deviceProxy.appendToApk(testBytes);

		verify(innerDeviceWrapperMock, times(1)).appendToAPK(testBytes);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = IOException.class)
	public void testAppendToApkFailed() throws RemoteException, IOException
	{
		byte[] testBytes = new byte[] {0, 1, 2, 3};
		Mockito.doThrow(new IOException()).when(innerDeviceWrapperMock).appendToAPK((byte[]) any());

		deviceProxy.appendToApk(testBytes);
	}

	@Test
	public void testBuildAndInstallApk() throws IOException, CommandFailedException
	{
		deviceProxy.buildAndInstallApk();

		verify(innerDeviceWrapperMock, times(1)).buildAndInstallAPK();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = IOException.class)
	public void testBuildAndInstallApkFailed() throws RemoteException, IOException, CommandFailedException
	{
		Mockito.doThrow(new IOException()).when(innerDeviceWrapperMock).buildAndInstallAPK();

		deviceProxy.buildAndInstallApk();
	}

	@Test
	public void testDiscardApk() throws RemoteException, IOException
	{
		deviceProxy.discardApk();

		verify(innerDeviceWrapperMock, times(1)).discardAPK();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = IOException.class)
	public void testDiscardApkFailed() throws RemoteException, IOException
	{
		Mockito.doThrow(new IOException()).when(innerDeviceWrapperMock).discardAPK();

		deviceProxy.discardApk();
	}

	@Test
	public void testGetUiXml() throws RemoteException, CommandFailedException
	{
		String testDataResponse = "testresponse";
		when(innerDeviceWrapperMock.getUiXml()).thenReturn(testDataResponse);

		String response = deviceProxy.getUiXml();

		assertEquals("Mocked response and actual method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getUiXml();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = CommandFailedException.class)
	public void testGetUiXmlFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.getUiXml()).thenThrow(new CommandFailedException());

		deviceProxy.getUiXml();
	}

	@Test
	public void testGetScreenShot() throws RemoteException, CommandFailedException
	{
		byte[] testDataResponse = new byte[] {0, 1, 2, 3};
		when(innerDeviceWrapperMock.getScreenshot()).thenReturn(testDataResponse);

		byte[] response = deviceProxy.getScreenshot();

		// converting arrays to lists, as assertEquals(array, array) is deprecated.
		assertEquals(	"Mocked response and actual method response should match.",
						Arrays.asList(testDataResponse),
						Arrays.asList(response));
		verify(innerDeviceWrapperMock, times(1)).getScreenshot();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetScreenShotFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.getScreenshot()).thenThrow(new RemoteException());

		deviceProxy.getScreenshot();
	}

	@Test
	public void testSetNetworkSpeed() throws RemoteException, CommandFailedException
	{
		Pair<Integer, Integer> testInput = new Pair<Integer, Integer>(0, 0);

		deviceProxy.setNetworkSpeed(testInput);

		verify(innerDeviceWrapperMock, times(1)).setNetworkSpeed(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetNetworkSpeedFailed() throws RemoteException, CommandFailedException
	{
		Pair<Integer, Integer> testInput = new Pair<Integer, Integer>(0, 0);
		Mockito.doThrow(new RemoteException())
				.when(innerDeviceWrapperMock)
				.setNetworkSpeed((Pair<Integer, Integer>) any());

		deviceProxy.setNetworkSpeed(testInput);
	}

	@Test
	public void testGetNetworkSpeed() throws RemoteException
	{
		Pair<Integer, Integer> testDataResponse = new Pair<Integer, Integer>(0, 0);
		when(innerDeviceWrapperMock.getNetworkSpeed()).thenReturn(testDataResponse);

		Pair<Integer, Integer> response = deviceProxy.getNetworkSpeed();

		assertEquals("Mocked response and actual method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getNetworkSpeed();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetNetworkSpeedFailed() throws RemoteException
	{
		when(innerDeviceWrapperMock.getNetworkSpeed()).thenThrow(new RemoteException());

		deviceProxy.getNetworkSpeed();
	}

	@Test
	public void testSetNetworkLatency() throws RemoteException
	{
		int testInput = 123;

		deviceProxy.setNetworkLatency(testInput);

		verify(innerDeviceWrapperMock, times(1)).setNetworkLatency(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetNetworkLatencyFailed() throws RemoteException
	{
		int testInput = 123;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setNetworkLatency(anyInt());

		deviceProxy.setNetworkLatency(testInput);
	}

	@Test
	public void testGetNetworkLatency() throws RemoteException
	{
		int testDataResponse = 123;
		when(innerDeviceWrapperMock.getNetworkLatency()).thenReturn(testDataResponse);

		int response = deviceProxy.getNetworkLatency();

		assertEquals("Input and response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getNetworkLatency();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetNetworkLatencyFailed() throws RemoteException
	{
		when(innerDeviceWrapperMock.getNetworkLatency()).thenThrow(new RemoteException());

		deviceProxy.getNetworkLatency();
	}

	@Test
	public void testSetBatteryLevel() throws RemoteException, CommandFailedException
	{
		int testInput = 123;

		deviceProxy.setBatteryLevel(testInput);

		verify(innerDeviceWrapperMock, times(1)).setBatteryLevel(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetBatteryLevelFailed() throws RemoteException, CommandFailedException
	{
		int testInput = 123;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setBatteryLevel(anyInt());

		deviceProxy.setBatteryLevel(testInput);
	}

	@Test
	public void testGetBatteryLevel() throws RemoteException, CommandFailedException
	{
		int testDataResponse = 123;
		when(innerDeviceWrapperMock.getBatteryLevel()).thenReturn(testDataResponse);

		int response = deviceProxy.getBatteryLevel();

		assertEquals("Expected response and method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getBatteryLevel();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetBatteryLevelFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.getBatteryLevel()).thenThrow(new RemoteException());

		deviceProxy.getBatteryLevel();
	}

	@Test
	public void testSetBatteryState() throws RemoteException, CommandFailedException
	{
		BatteryState testInput = BatteryState.CHARGING;

		deviceProxy.setBatteryState(testInput);

		verify(innerDeviceWrapperMock, times(1)).setBatteryState(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetBatteryStateFailed() throws RemoteException, CommandFailedException
	{
		BatteryState testInput = BatteryState.CHARGING;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setBatteryState((BatteryState) any());

		deviceProxy.setBatteryState(testInput);
	}

	@Test
	public void testGetBatteryState() throws RemoteException, CommandFailedException
	{
		BatteryState testDataResponse = BatteryState.CHARGING;
		when(innerDeviceWrapperMock.getBatteryState()).thenReturn(testDataResponse);

		BatteryState response = deviceProxy.getBatteryState();

		assertEquals("Expected response and method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getBatteryState();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetBatteryStateFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.getBatteryState()).thenThrow(new RemoteException());

		deviceProxy.getBatteryState();
	}

	@Test
	public void testSetPowerState() throws CommandFailedException, RemoteException
	{
		boolean testInput = false;

		deviceProxy.setPowerState(testInput);

		verify(innerDeviceWrapperMock, times(1)).setPowerState(testInput);
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testSetPowerStateFailed() throws RemoteException, CommandFailedException
	{
		boolean testInput = false;
		Mockito.doThrow(new RemoteException()).when(innerDeviceWrapperMock).setPowerState((Boolean) any());

		deviceProxy.setPowerState(testInput);
	}

	@Test
	public void testGetPowerState() throws RemoteException, CommandFailedException
	{
		boolean testDataResponse = false;
		when(innerDeviceWrapperMock.getPowerState()).thenReturn(testDataResponse);

		boolean response = deviceProxy.getPowerState();

		assertEquals("Expected response and method response should match.", testDataResponse, response);
		verify(innerDeviceWrapperMock, times(1)).getPowerState();
		verifyNoMoreInteractions(innerDeviceWrapperMock);
	}

	@Test(expected = RuntimeException.class)
	public void testGetPowerStateFailed() throws RemoteException, CommandFailedException
	{
		when(innerDeviceWrapperMock.getPowerState()).thenThrow(new RemoteException());

		deviceProxy.getPowerState();
	}

}
