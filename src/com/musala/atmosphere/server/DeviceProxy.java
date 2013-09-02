package com.musala.atmosphere.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.DeviceAcceleration;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.ScreenOrientation;
import com.musala.atmosphere.commons.cs.InvalidPasskeyException;
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
	private static final long serialVersionUID = -2645952387036199816L;

	private final IWrapDevice wrappedDevice;

	private DeviceInformation deviceInformation;

	private PasskeyAuthority passkeyAuthority;

	public DeviceProxy(IWrapDevice deviceToWrap) throws RemoteException
	{
		wrappedDevice = deviceToWrap;
		passkeyAuthority = PasskeyAuthority.getInstance();
	}

	@Override
	public String executeShellCommand(String command, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			String returnValue = wrappedDevice.executeShellCommand(command);
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public long getFreeRam(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			long returnValue = wrappedDevice.getFreeRAM();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public List<String> executeSequenceOfShellCommands(List<String> commands, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			List<String> returnValue = wrappedDevice.executeSequenceOfShellCommands(commands);
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void initApkInstall(long invocationPasskey) throws RemoteException, IOException, InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.initAPKInstall();
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void appendToApk(byte[] bytes, long invocationPasskey)
		throws RemoteException,
			IOException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.appendToAPK(bytes);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void buildAndInstallApk(long invocationPasskey)
		throws IOException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.buildAndInstallAPK();
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void discardApk(long invocationPasskey) throws RemoteException, InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.discardAPK();
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public String getUiXml(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			String returnValue = wrappedDevice.getUiXml();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public byte[] getScreenshot(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			byte[] returnValue = wrappedDevice.getScreenshot();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setNetworkSpeed(speeds);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public Pair<Integer, Integer> getNetworkSpeed(long invocationPasskey)
		throws RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			Pair<Integer, Integer> returnValue = wrappedDevice.getNetworkSpeed();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setNetworkLatency(int latency, long invocationPasskey) throws RemoteException, InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setNetworkLatency(latency);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}

	}

	@Override
	public int getNetworkLatency(long invocationPasskey) throws RemoteException, InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			int returnValue = wrappedDevice.getNetworkLatency();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setBatteryLevel(int level, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setBatteryLevel(level);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public int getBatteryLevel(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			int returnValue = wrappedDevice.getBatteryLevel();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setBatteryState(BatteryState state, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setBatteryState(state);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public BatteryState getBatteryState(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			BatteryState returnValue = wrappedDevice.getBatteryState();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public boolean getPowerState(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			boolean powerState = wrappedDevice.getPowerState();
			return powerState;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setPowerState(boolean state, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setPowerState(state);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setAirplaneMode(boolean airplaneMode, long invocationPasskey)
		throws CommandFailedException,
			RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setAirplaneMode(airplaneMode);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed", e);
		}
	}

	@Override
	public DeviceInformation getDeviceInformation(long invocationPasskey)
		throws RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		deviceInformation = wrappedDevice.getDeviceInformation();
		return deviceInformation;
	}

	@Override
	public void setDeviceOrientation(DeviceOrientation deviceOrientation, long invocationPasskey)
		throws CommandFailedException,
			RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setDeviceOrientation(deviceOrientation);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed", e);
		}
	}

	@Override
	public DeviceOrientation getDeviceOrientation(long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			DeviceOrientation deviceOrientation = wrappedDevice.getDeviceOrientation();
			return deviceOrientation;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}

	}

	@Override
	public void setScreenOrientation(ScreenOrientation screenOrientation, long invocationPasskey)
		throws RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setScreenOrientation(screenOrientation);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed", e);
		}
	}

	@Override
	public void setAutoRotation(boolean autoRotation, long invocationPasskey)
		throws RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setAutoRotation(autoRotation);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed", e);
		}
	}

	@Override
	public void setAcceleration(DeviceAcceleration deviceAcceleration, long invocationPasskey)
		throws CommandFailedException,
			RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			wrappedDevice.setAcceleration(deviceAcceleration);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed", e);
		}
	}

	@Override
	public DeviceAcceleration getDeviceAcceleration(long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		try
		{
			DeviceAcceleration deviceAcceleration = wrappedDevice.getDeviceAcceleration();
			return deviceAcceleration;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}

	}

}
