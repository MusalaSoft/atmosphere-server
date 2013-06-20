package com.musala.atmosphere.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.cs.clientdevice.BatteryState;
import com.musala.atmosphere.commons.cs.clientdevice.IClientDevice;
import com.musala.atmosphere.commons.sa.IWrapDevice;

/**
 * The DeviceProxy object is used in RMI. It reroutes invocations of it's methods (by the {@link IClientDevice
 * IClientDevice} stub) to invocations on another RMI stub (the {@link IWrapDevice IWrapDevice}).
 * 
 * @author georgi.gaydarov
 * 
 */
public class DeviceProxy extends UnicastRemoteObject implements IClientDevice
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2645952387036199816L;

	private IWrapDevice wrappedDevice;

	public DeviceProxy(IWrapDevice deviceToWrap) throws RemoteException
	{
		wrappedDevice = deviceToWrap;
	}

	@Override
	public String executeShellCommand(String command) throws RemoteException
	{
		// TODO Implement executeshellcommand in the deviceproxy
		return null;
	}

	@Override
	public int getFreeRam() throws RemoteException
	{
		// TODO Implement getfreeram in the deviceproxy
		return 0;
	}

	@Override
	public void executeSequenceOfShellCommands(List<String> commands) throws RemoteException
	{
		// TODO Implement executesequenceofshellcommands in the deviceproxy
	}

	@Override
	public void initApkInstall() throws RemoteException
	{
		// TODO Implement initpakinstall in the deviceproxy
	}

	@Override
	public void appendToApk(Byte[] bytes) throws RemoteException
	{
		// TODO Implement appendtoapk in the deviceproxy
	}

	@Override
	public void buildAndInstallApk() throws RemoteException
	{
		// TODO Implement buildandinstallapk in the deviceproxy
	}

	@Override
	public void discardApk() throws RemoteException
	{
		// TODO Implement discardapk in the deviceproxy
	}

	@Override
	public String getUiXml() throws RemoteException
	{
		// TODO Implement getuixml in the deviceproxy
		return null;
	}

	@Override
	public Byte[] getScreenShot() throws RemoteException
	{
		// TODO Implement getscreenshot in the deviceproxy
		return null;
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException
	{
		// TODO Implement setnetworkspeed in the deviceproxy

	}

	@Override
	public Pair<Integer, Integer> getNetworkSpeed() throws RemoteException
	{
		// TODO Implement getnetworkspeed in the deviceproxy
		return null;
	}

	@Override
	public void setNetworkLatency(int latency) throws RemoteException
	{
		// TODO Implement setnetworklatency in the deviceproxy

	}

	@Override
	public int getNetworkLatency() throws RemoteException
	{
		// TODO Implement getnetworklatency in the deviceproxy
		return 0;
	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException
	{
		// TODO Implement setbatterylevel in the deviceproxy
	}

	@Override
	public int getBatteryLevel() throws RemoteException
	{
		// TODO Implement getbatterylevel in the deviceproxy
		return 0;
	}

	@Override
	public void setBatteryState(BatteryState state) throws RemoteException
	{
		// TODO Implement setbatterystate in the deviceproxy

	}

	@Override
	public BatteryState getBatteryState() throws RemoteException
	{
		// TODO Implement getbatterystate in the deviceproxy
		return null;
	}
}
