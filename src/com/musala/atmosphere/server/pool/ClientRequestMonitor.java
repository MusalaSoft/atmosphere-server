package com.musala.atmosphere.server.pool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

/**
 * Class that monitors the activity of all devices on the server. It tracks devices that are allocated to Client but are
 * not used, and releases them after some configurable time.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class ClientRequestMonitor
{
	private static final Logger LOGGER = Logger.getLogger(ClientRequestMonitor.class.getCanonicalName());

	private static ConcurrentHashMap<DeviceProxy, Long> proxyToTimeout = new ConcurrentHashMap<DeviceProxy, Long>();

	private static ConcurrentHashMap<DeviceProxy, String> proxyToRmiId = new ConcurrentHashMap<DeviceProxy, String>();

	private Thread monitorThread;

	private static ClientRequestMonitor monitorInstance;

	private static PoolManager poolManager;

	private ClientRequestMonitor()
	{
		poolManager = PoolManager.getInstance();
		registerAvailableDevices();
		startMonitorThread();
		LOGGER.info("ClientRequestMonitor instance created successfully.");
	}

	/**
	 * Gets the current instance of the {@link ClientRequestMonitor ClientRequestMonitor}.
	 * 
	 * @return the only Instance of the ClientRequestMonitor.
	 */
	public static ClientRequestMonitor getInstance()
	{
		if (monitorInstance == null)
		{
			synchronized (ClientRequestMonitor.class)
			{
				if (monitorInstance == null)
				{
					monitorInstance = new ClientRequestMonitor();
				}
			}
		}
		return monitorInstance;
	}

	private void registerAvailableDevices()
	{
		// get all available devices' RMI identifiers
		List<String> allDevicesRmiIdentifiers = poolManager.getAllUnderlyingDeviceProxyIds();

		// registering devices to the ClientRequestMonitor
		for (String deviceRmiIdentifier : allDevicesRmiIdentifiers)
		{
			DeviceProxy deviceProxy = poolManager.getUnderlyingDeviceProxy(deviceRmiIdentifier);
			proxyToTimeout.put(deviceProxy, 0L);
			proxyToRmiId.put(deviceProxy, deviceRmiIdentifier);
		}
	}

	private void startMonitorThread()
	{
		InnerRunnable innerThread = new InnerRunnable();
		monitorThread = new Thread(innerThread, "ClientRequestMonitor Thread");
		monitorThread.start();
	}

	/**
	 * This method registers a device for monitoring when it is attached to an Agent.
	 * 
	 * @param poolItemRmiIdentifier
	 *        - RMI binding identifier for newly attached {@link PoolItem PoolItem}
	 */
	void registerDevice(String poolItemRmiIdentifier)
	{
		DeviceProxy deviceProxy = poolManager.getUnderlyingDeviceProxy(poolItemRmiIdentifier);
		proxyToTimeout.putIfAbsent(deviceProxy, 0L);
		proxyToRmiId.putIfAbsent(deviceProxy, poolItemRmiIdentifier);

		LOGGER.info("ClientRequestMonitor registered new device with RMI ID: " + poolItemRmiIdentifier);
	}

	/**
	 * This method removes a device from the {@link ClientRequestMonitor ClientRequestMonitor}.
	 * 
	 * @param poolItemRmiIdentifier
	 *        - RMI id of detached device
	 */
	void unregisterDevice(String poolItemRmiIdentifier)
	{
		DeviceProxy deviceProxy = poolManager.getUnderlyingDeviceProxy(poolItemRmiIdentifier);
		boolean isDeviceProxyRegistered = (proxyToTimeout.contains(deviceProxy) && proxyToRmiId.contains(deviceProxy));
		if (isDeviceProxyRegistered)
		{
			proxyToTimeout.remove(deviceProxy);
			proxyToRmiId.remove(deviceProxy);
			LOGGER.info("ClientRequestMonitor unregistered device: " + poolItemRmiIdentifier);
		}
		else
		{
			// it was not found in some of the maps for a reason
			LOGGER.error("Trying to unregister device " + poolItemRmiIdentifier
					+ " which was not registered for monitoring.");
		}
	}

	/**
	 * Restarts timeout value for given device. This method should be invoked in each DeviceProxy method.
	 * 
	 * @param selectedDeviceProxy
	 *        - active {@link DeviceProxy DeviceProxy}
	 */
	public void restartTimerForDevice(DeviceProxy selectedDeviceProxy)
	{
		String rmiId = proxyToRmiId.get(selectedDeviceProxy);
		if (poolManager.isInUse(rmiId))
		{
			proxyToTimeout.put(selectedDeviceProxy, 0L);
		}
	}

	/**
	 * Stops the ClientRequestMonitor. This method is invoked when the Server is stopped.
	 */
	public void stop()
	{
		try
		{
			InnerRunnable.setTerminateFlag(true);
			monitorThread.join();
			LOGGER.info("ClientRequestMonitor stopped successfully.");
		}
		catch (InterruptedException e)
		{
			LOGGER.error("Monitoring could not be stopped.", e);
		}
	}

	/**
	 * 
	 * @return true if the ClientRequestMonitor is running, false otherwise.
	 */
	public boolean isRunning()
	{
		boolean isRunning = monitorThread.isAlive();
		return isRunning;
	}

	/**
	 * Inner runnable class for the ClientRequestMonitor. It holds all the necessary logic for regularly updating the
	 * timeout values of all registered devices.
	 * 
	 * @author vladimir.vladimirov
	 * 
	 */
	private static class InnerRunnable implements Runnable
	{
		private final int DEVICE_REQUEST_TIMEOUT = ServerPropertiesLoader.getDeviceRequestTimeout();

		private final int DEVICE_UPDATE_SLEEP = ServerPropertiesLoader.getDeviceUpdateTime();

		private static boolean terminateFlag = false;

		@Override
		public void run()
		{
			try
			{
				while (!terminateFlag)
				{
					Thread.sleep(DEVICE_UPDATE_SLEEP);
					updateTimeoutValues();
				}
			}
			catch (InterruptedException e)
			{
				LOGGER.error("Monitor thread was interrupted.", e);
				throw new RuntimeException("ClientRequestMonitor thread was interrupted.");
			}
		}

		/**
		 * Sets the termination flag on this runnable.
		 * 
		 * @param terminate
		 *        - true if this runnable should exit, false otherwise.
		 */
		public static void setTerminateFlag(boolean terminate)
		{
			terminateFlag = terminate;
		}

		/**
		 * Checks out all devices for timeouts and updates the map with timeout values. If some device is timed out,
		 * it's been released.
		 */
		private void updateTimeoutValues()
		{
			final long TIMEOUT_STEP = 1L;

			for (Map.Entry<DeviceProxy, Long> entry : proxyToTimeout.entrySet())
			{
				DeviceProxy currentDeviceProxy = entry.getKey();
				long timeout = entry.getValue();
				String rmiId = proxyToRmiId.get(currentDeviceProxy);

				if (poolManager.isInUse(rmiId))
				{
					if (timeout >= DEVICE_REQUEST_TIMEOUT)
					{
						LOGGER.info("Device proxy with RMI ID: " + rmiId + " released due to invocation timeout.");
						entry.setValue(0L);
						poolManager.releasePoolItem(rmiId);
					}
					else
					{
						long newTimeout = entry.getValue() + TIMEOUT_STEP;
						entry.setValue(newTimeout);
					}
				}
				else
				{
					entry.setValue(0L);
				}
			}
		}
	}
}
