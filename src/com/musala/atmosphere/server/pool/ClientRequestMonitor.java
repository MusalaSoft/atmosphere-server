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

	private ConcurrentHashMap<DeviceProxy, Long> proxyToTimeout;

	private ConcurrentHashMap<DeviceProxy, String> proxyToRmiId;

	private Thread monitorThread;

	private boolean isRunning = false;

	private static final ClientRequestMonitor monitorInstance = new ClientRequestMonitor();

	private PoolManager poolManager = PoolManager.getInstance();

	private ClientRequestMonitor()
	{
		registerAvailableDevices();
		startMonitorThread();
		LOGGER.info("ClientRequestMonitor instance created successfully.");
	}

	/**
	 * Gets the current instance of the {@link ClientRequestMonitor ClientRequestMonitor}.
	 * 
	 * @return - the only Instance of the ClientRequestMonitor.
	 */
	public static ClientRequestMonitor getInstance()
	{
		return monitorInstance;
	}

	private void registerAvailableDevices()
	{
		proxyToTimeout = new ConcurrentHashMap<DeviceProxy, Long>();
		proxyToRmiId = new ConcurrentHashMap<DeviceProxy, String>();

		// get all available devices' RMI identifiers
		PoolManager poolManager = PoolManager.getInstance();
		List<String> allDevicesRmiIdentifiers = poolManager.getAllUnderlyingDeviceProxyIds();

		// registering devices to the ClientRequestMonitor
		for (String deviceRmiIdentifier : allDevicesRmiIdentifiers)
		{
			DeviceProxy deviceProxy = getProxyByRmiId(deviceRmiIdentifier);
			proxyToTimeout.put(deviceProxy, 0L);
			proxyToRmiId.put(deviceProxy, deviceRmiIdentifier);
		}
	}

	private void startMonitorThread()
	{
		isRunning = true;
		InnerRunnable innerThread = new InnerRunnable();
		monitorThread = new Thread(innerThread, "ClientRequestMonitorThread");
		monitorThread.start();
	}

	private DeviceProxy getProxyByRmiId(String deviceRmiId)
	{
		DeviceProxy deviceProxy = poolManager.getUnderlyingDeviceProxy(deviceRmiId);
		return deviceProxy;
	}

	/**
	 * This method registers a device for monitoring when it is attached to an Agent.
	 * 
	 * @param poolItemRmiIdentifier
	 *        - RMI binding identifier for newly attached {@link PoolItem PoolItem}
	 */
	void registerDevice(String poolItemRmiIdentifier)
	{
		DeviceProxy deviceProxy = getProxyByRmiId(poolItemRmiIdentifier);
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
		DeviceProxy deviceProxy = getProxyByRmiId(poolItemRmiIdentifier);
		boolean isDeviceProxyPresent = (proxyToTimeout.contains(deviceProxy) && proxyToRmiId.contains(deviceProxy));
		if (isDeviceProxyPresent)
		{
			LOGGER.info("ClientRequestMonitor lost connection to device: " + poolItemRmiIdentifier);
		}
		else
		{
			// it was not found in some of the maps for a reason
			LOGGER.error("Trying to unregister device " + poolItemRmiIdentifier
					+ " which was not registered for monitoring properly.");
		}
		proxyToTimeout.remove(deviceProxy);
		proxyToRmiId.remove(deviceProxy);
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
			isRunning = false;
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
	 * @return - true, if the ClientRequestMonitor is running, false otherwise.
	 */
	public boolean isRunning()
	{
		return isRunning;
	}

	/**
	 * Inner runnable class for the ClientRequestMonitor. It holds all the necessary logic for regularly updating the
	 * timeout values of all registered devices.
	 * 
	 * @author vladimir.vladimirov
	 * 
	 */
	private class InnerRunnable implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				while (isRunning)
				{
					Thread.sleep(ServerPropertiesLoader.getDeviceUpdateTime());
					updateTimeoutValues();
				}
			}
			catch (InterruptedException e)
			{
				LOGGER.error("Monitor thread was interrupted.", e);
				throw new RuntimeException("ClientRequestMonitor thread was interrupted");
			}
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
					if (timeout >= ServerPropertiesLoader.getDeviceRequestTimeout())
					{
						poolManager.releasePoolItem(rmiId);
					}
					else
					{
						long newTimeout = entry.getValue() + TIMEOUT_STEP;
						entry.setValue(newTimeout);
					}
				}
			}
		}
	}
}
