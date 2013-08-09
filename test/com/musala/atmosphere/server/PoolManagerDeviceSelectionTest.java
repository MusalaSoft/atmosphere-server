package com.musala.atmosphere.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.NoSuchElementException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.server.pool.PoolManager;

public class PoolManagerDeviceSelectionTest
{
	private final static int POOL_MANAGER_RMI_PORT = 1234;

	private final static String AGENT_ID = "mockagent";

	private final static String DEVICE1_SN = "mockdevice1";

	private final static String DEVICE2_SN = "mockdevice2";

	private final static String DEVICE3_SN = "mockdevice3";

	private final static String DEVICE4_SN = "mockdevice4";

	private static ServerManager serverManager;

	private static PoolManager poolManager;

	@BeforeClass
	public static void setUp() throws Exception
	{
		serverManager = new ServerManager(POOL_MANAGER_RMI_PORT);

		poolManager = PoolManager.getInstance(serverManager);

		IAgentManager mockedAgentManager = mock(IAgentManager.class);
		when(mockedAgentManager.getAgentId()).thenReturn(AGENT_ID);

		IWrapDevice mockedDeviceOne = mock(IWrapDevice.class);
		IWrapDevice mockedDeviceTwo = mock(IWrapDevice.class);
		IWrapDevice mockedDeviceThree = mock(IWrapDevice.class);
		IWrapDevice mockedDeviceFour = mock(IWrapDevice.class);

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
		mockedDeviceInfoFour.setEmulator(false);
		mockedDeviceInfoFour.setRam(512);
		mockedDeviceInfoFour.setResolution(new Pair<>(200, 200));
		mockedDeviceInfoFour.setDpi(180);

		when(mockedDeviceOne.getDeviceInformation()).thenReturn(mockedDeviceInfoOne);
		when(mockedDeviceTwo.getDeviceInformation()).thenReturn(mockedDeviceInfoTwo);
		when(mockedDeviceThree.getDeviceInformation()).thenReturn(mockedDeviceInfoThree);
		when(mockedDeviceFour.getDeviceInformation()).thenReturn(mockedDeviceInfoFour);

		poolManager.addDevice(DEVICE1_SN, mockedDeviceOne, mockedAgentManager, POOL_MANAGER_RMI_PORT);
		poolManager.addDevice(DEVICE2_SN, mockedDeviceTwo, mockedAgentManager, POOL_MANAGER_RMI_PORT);
		poolManager.addDevice(DEVICE3_SN, mockedDeviceThree, mockedAgentManager, POOL_MANAGER_RMI_PORT);
		poolManager.addDevice(DEVICE4_SN, mockedDeviceFour, mockedAgentManager, POOL_MANAGER_RMI_PORT);
	}

	@AfterClass
	public static void tearDown() throws Exception
	{
		serverManager.close();
	}

	@Test(expected = NoSuchElementException.class)
	public void getNotPresentDevice() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setOs(DeviceOs.JELLY_BEAN_MR1_4_2_1);
		parameters.setRam(256);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
	}

	@Test
	public void getPresentDeviceFirstTestOne() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setRam(128);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
		assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + DEVICE1_SN, rmiId);
		poolManager.releaseDevice(rmiId);
	}

	@Test
	public void getPresentDeviceFirstTestTwo() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setDeviceType(DeviceType.EMULATOR_PREFERRED);
		parameters.setResolutionHeight(600);
		parameters.setResolutionWidth(800);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
		assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + DEVICE1_SN, rmiId);
		poolManager.releaseDevice(rmiId);
	}

	@Test
	public void getPresentDeviceFirstTestThree() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setRam(128);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
		assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + DEVICE1_SN, rmiId);
		poolManager.releaseDevice(rmiId);
	}

	@Test
	public void getPresentDeviceSecond() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setDpi(240);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
		assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + DEVICE2_SN, rmiId);
	}

	@Test
	public void getPresentDeviceThird() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setDeviceType(DeviceType.DEVICE_ONLY);
		parameters.setRam(512);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
		assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + DEVICE3_SN, rmiId);
	}

	@Test
	public void getPresentDeviceFourth() throws RemoteException
	{
		DeviceParameters parameters = new DeviceParameters();
		parameters.setDpi(180);

		String rmiId = poolManager.getDeviceProxyRmiId(parameters);
		assertEquals("Failed to receive RMI ID of the correct device.", AGENT_ID + "_" + DEVICE4_SN, rmiId);
	}

}
