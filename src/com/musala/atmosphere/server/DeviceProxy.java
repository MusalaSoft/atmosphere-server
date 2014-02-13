package com.musala.atmosphere.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.TelephonyInformation;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.cs.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.clientdevice.IClientDevice;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Gesture;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;

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

	private ClientRequestMonitor timeoutMonitor = ClientRequestMonitor.getInstance();

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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
	public void appendToApk(byte[] bytes, long invocationPasskey, int length)
		throws RemoteException,
			IOException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.appendToAPK(bytes, length);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
	public void setPowerProperties(PowerProperties properties, long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.setPowerProperties(properties);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public PowerProperties getPowerProperties(long invocationPasskey)
		throws RemoteException,
			CommandFailedException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			PowerProperties returnValue = wrappedDevice.getPowerProperties();
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
	public DeviceInformation getDeviceInformation(long invocationPasskey)
		throws RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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
	public void setAcceleration(DeviceAcceleration deviceAcceleration, long invocationPasskey)
		throws CommandFailedException,
			RemoteException,
			InvalidPasskeyException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
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
		timeoutMonitor.restartTimerForDevice(this);
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

	@Override
	public void setMobileDataState(MobileDataState state, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.setMobileDataState(state);
		}
		catch (RemoteException e)
		{
			// TODO handle RemotException (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}

	}

	@Override
	public ConnectionType getConnectionType(long invocationPasskey)
		throws InvalidPasskeyException,
			RemoteException,
			CommandFailedException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			ConnectionType connectionType = wrappedDevice.getConnectionType();
			return connectionType;
		}
		catch (RemoteException e)
		{
			// TODO handle remoteexception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public MobileDataState getMobileDataState(long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			MobileDataState state = wrappedDevice.getMobileDataState();
			return state;
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void setWiFi(boolean state, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.setWiFi(state);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}

	}

	@Override
	public void receiveSms(SmsMessage smsMessage, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.receiveSms(smsMessage);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void executeGesture(Gesture gesture, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.executeGesture(gesture);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void receiveCall(PhoneNumber phoneNumber, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.receiveCall(phoneNumber);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void acceptCall(PhoneNumber phoneNumber, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{

		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.acceptCall(phoneNumber);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void holdCall(PhoneNumber phoneNumber, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.holdCall(phoneNumber);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public void cancelCall(PhoneNumber phoneNumber, long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			wrappedDevice.cancelCall(phoneNumber);
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}

	@Override
	public TelephonyInformation getTelephonyInformation(long invocationPasskey)
		throws InvalidPasskeyException,
			CommandFailedException,
			RemoteException
	{
		passkeyAuthority.validatePasskey(this, invocationPasskey);
		timeoutMonitor.restartTimerForDevice(this);
		try
		{
			return wrappedDevice.getTelephonyInformation();
		}
		catch (RemoteException e)
		{
			// TODO handle remote exception (the server should know the connection is bad)
			// and decide what to do next. This next line is temporal.
			throw new RuntimeException("Connection to device failed.", e);
		}
	}
}
