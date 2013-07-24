package com.musala.atmosphere.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.Pair;
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

	private IWrapDevice wrappedDevice;

	public DeviceProxy(IWrapDevice deviceToWrap) throws RemoteException
	{
		wrappedDevice = deviceToWrap;
	}

	@Override
	public String executeShellCommand(String command) throws RemoteException, CommandFailedException
	{
		try
		{
			String returnValue = wrappedDevice.executeShellCommand(command);
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public long getFreeRam() throws RemoteException, CommandFailedException
	{
		try
		{
			long returnValue = wrappedDevice.getFreeRAM();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public List<String> executeSequenceOfShellCommands(List<String> commands)
		throws RemoteException,
			CommandFailedException
	{
		try
		{
			List<String> returnValue = wrappedDevice.executeSequenceOfShellCommands(commands);
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void initApkInstall() throws RemoteException, IOException
	{
		try
		{
			wrappedDevice.initAPKInstall();
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void appendToApk(byte[] bytes) throws RemoteException, IOException
	{
		try
		{
			wrappedDevice.appendToAPK(bytes);
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void buildAndInstallApk() throws IOException, CommandFailedException
	{
		try
		{
			wrappedDevice.buildAndInstallAPK();
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void discardApk() throws RemoteException, IOException
	{
		try
		{
			wrappedDevice.discardAPK();
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public String getUiXml() throws RemoteException, CommandFailedException
	{
		try
		{
			String returnValue = wrappedDevice.getUiXml();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	// TODO Byte[] will most likely be changed to byte[] when this method is implemented in the AbstractWrapDevice
	@Override
	public byte[] getScreenshot() throws RemoteException, CommandFailedException
	{
		try
		{
			byte[] returnValue = wrappedDevice.getScreenshot();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException, CommandFailedException
	{
		try
		{
			wrappedDevice.setNetworkSpeed(speeds);
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public Pair<Integer, Integer> getNetworkSpeed() throws RemoteException
	{
		try
		{
			Pair<Integer, Integer> returnValue = wrappedDevice.getNetworkSpeed();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setNetworkLatency(int latency) throws RemoteException
	{
		try
		{
			wrappedDevice.setNetworkLatency(latency);
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}

	}

	@Override
	public int getNetworkLatency() throws RemoteException
	{
		try
		{
			int returnValue = wrappedDevice.getNetworkLatency();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException, CommandFailedException
	{
		try
		{
			wrappedDevice.setBatteryLevel(level);
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public int getBatteryLevel() throws RemoteException, CommandFailedException
	{
		try
		{
			int returnValue = wrappedDevice.getBatteryLevel();
			return returnValue;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setBatteryState(BatteryState state) throws RemoteException, CommandFailedException
	{
		try
		{
			wrappedDevice.setBatteryState(state);
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public BatteryState getBatteryState() throws RemoteException, CommandFailedException
	{
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
	public boolean getPowerState() throws RemoteException, CommandFailedException
	{
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
	public void setPowerState(boolean state) throws RemoteException, CommandFailedException
	{
		try
		{
			wrappedDevice.setPowerState(state);
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}
}
