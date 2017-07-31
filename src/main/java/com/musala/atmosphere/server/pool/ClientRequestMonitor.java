package com.musala.atmosphere.server.pool;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;

/**
 * Class that monitors the activity of all devices on the server. It tracks devices that are allocated to Client but are
 * not used, and releases them after some configurable time.
 *
 * @author vladimir.vladimirov
 *
 */
public class ClientRequestMonitor {
    private static final Logger LOGGER = Logger.getLogger(ClientRequestMonitor.class.getCanonicalName());

    private static final Long STARTING_TIMEOUT = 0L;

    private static HashMap<String, Long> deviceIdToTimeout = new HashMap<>();

    private static Thread monitorThread;

    private static PoolManager poolManager = PoolManager.getInstance();

    static {
        InnerRunnable innerThread = new InnerRunnable();
        monitorThread = new Thread(innerThread, "ClientRequestMonitor Thread");
        monitorThread.start();

        LOGGER.info("ClientRequestMonitor instance created successfully.");
    }

    /**
     * This method registers a device for monitoring when it is attached to an Agent.
     *
     * @param deviceId
     *        - the device ID
     */
    void registerDevice(String deviceId) {
        deviceIdToTimeout.put(deviceId, STARTING_TIMEOUT);

        String message = String.format("ClientRequestMonitor registered new device with ID %s.", deviceId);
        LOGGER.info(message);
    }

    /**
     * This method removes a device from the {@link ClientRequestMonitor ClientRequestMonitor}.
     *
     * @param deviceId
     *        - the device ID
     */
    void unregisterDevice(String deviceId) {
        String message;

        if (deviceIdToTimeout.containsKey(deviceId)) {
            deviceIdToTimeout.remove(deviceId);
            message = String.format("ClientRequestMonitor unregistered device with ID %s", deviceId);
        } else {
            message = String.format("Trying to unregister device %s which was not registered for monitoring.",
                                    deviceId);
        }

        LOGGER.info(message);
    }

    /**
     * Restarts timeout value for given device.
     *
     * @param deviceId
     *        - the device ID
     */
    public void restartTimerForDevice(String deviceId) {
        deviceIdToTimeout.put(deviceId, STARTING_TIMEOUT);
    }

    /**
     * Stops the ClientRequestMonitor. This method is invoked when the Server is stopped.
     */
    public void stop() {
        try {
            InnerRunnable.setTerminateFlag(true);
            monitorThread.join();
            LOGGER.info("ClientRequestMonitor stopped successfully.");
        } catch (InterruptedException e) {
            LOGGER.error("Monitoring could not be stopped.", e);
        }
    }

    /**
     * Inner runnable class for the ClientRequestMonitor. It holds all the necessary logic for regularly updating the
     * timeout values of all registered devices.
     *
     * @author vladimir.vladimirov
     *
     */
    private static class InnerRunnable implements Runnable {
        private final int DEVICE_REQUEST_TIMEOUT = ServerPropertiesLoader.getDeviceRequestTimeout();

        private final int DEVICE_UPDATE_SLEEP = ServerPropertiesLoader.getDeviceUpdateTime();

        private static final long TIMEOUT_STEP = 1L;

        private static boolean terminateFlag = false;

        @Override
        public void run() {
            while (!terminateFlag) {
                try {
                    Thread.sleep(DEVICE_UPDATE_SLEEP);
                    updateTimeoutValues();
                } catch (InterruptedException | RemoteException e) {
                    LOGGER.error("Monitor thread was interrupted.", e);
                    throw new RuntimeException("ClientRequestMonitor thread was interrupted.");
                }
            }
        }

        /**
         * Sets the termination flag on this runnable.
         *
         * @param terminate
         *        - true if this runnable should exit, false otherwise.
         */
        public static void setTerminateFlag(boolean terminate) {
            terminateFlag = terminate;
        }

        /**
         * Checks out all devices for timeouts and updates the map with timeout values. If some device is timed out,
         * it's been released.
         *
         * @throws RemoteException
         *         - thrown when an error during the execution of a remote method call.
         */
        private void updateTimeoutValues() throws RemoteException {
            Iterator<Entry<String, Long>> iterator = deviceIdToTimeout.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, Long> entry = iterator.next();
                String deviceId = entry.getKey();
                long timeout = entry.getValue();

                if (timeout >= DEVICE_REQUEST_TIMEOUT) {
                    String message = String.format("Device with ID %s released due to invocation timeout.",
                                                   deviceId);
                    LOGGER.info(message);

                    try {
                        poolManager.releaseDevice(deviceId);
                        iterator.remove();
                    } catch (DevicePoolDaoException e) {
                        String errorMessage = String.format("Releasing device due to invocation timeout with ID %s failed.",
                                                            deviceId);
                        LOGGER.error(errorMessage);
                    }
                } else {
                    long newTimeout = entry.getValue() + TIMEOUT_STEP;
                    entry.setValue(newTimeout);
                }
            }
        }
    }
}
