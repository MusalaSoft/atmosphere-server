package com.musala.atmosphere.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.NoDeviceMatchingTheGivenSelectorException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.eventservice.event.device.allocate.DeviceReleasedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Manages all device allocations. If a certain device is not available the client will wait for an appropriate device.
 *
 * @author dimcho.nedev
 *
 */
public final class DeviceAllocationManager implements Subscriber {
    private static Logger LOGGER = Logger.getLogger(DeviceAllocationManager.class.getCanonicalName());

    private static final int WAIT_FOR_OTHER_CLIENTS_TIMEOUT = 10_000; // milliseconds

    private static final int COUNTDOWN_NUMBER = 1;

    private IDevicePoolDao devicePoolDao;

    private List<Pair<DeviceSelector, String>> waitingClients = Collections.synchronizedList(new ArrayList<>());

    private Map<String, CountDownLatch> clientIdForLatch = new ConcurrentHashMap<>();

    private Map<String, DeviceAllocationInformation> clientIdForDeviceAllocationInformation = new ConcurrentHashMap<>();

    private DeviceSelectorApplicabilityChecker slectorResolver;

    private CountDownLatch availableDeviceLatch;

    public DeviceAllocationManager(IDevicePoolDao devicePoolDao) {
        this.devicePoolDao = devicePoolDao;
        slectorResolver = new DeviceSelectorApplicabilityChecker();
    }

    /**
     * Allocates a device matched by a given selector.
     *
     * @param deviceSelector
     *        - a {@link DeviceSelector selector} that match a specific device.
     * @param clientId
     *        - a unique identifier of the client session
     * @param waitForDeviceTimeout
     *        - the maximum time the client will wait for an appropriate device     
     * @return an {@link DeviceAllocationInformation information} for the allocated device
     */
    public DeviceAllocationInformation allocateDevice(DeviceSelector deviceSelector, String clientId, final int waitForDeviceTimeout) {
        // the client will wait for the other allocation requests if a new device is available for use or till the
        // timeout period expired.
        if (availableDeviceLatch != null && availableDeviceLatch.getCount() > 0) {
            // there is a new available device(published or released)
            try {
                availableDeviceLatch.await(WAIT_FOR_OTHER_CLIENTS_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Waiting for action is interrupted.", e);
            }
        }

        DeviceAllocationInformation dAllocInfo = allocate(deviceSelector);

        if (dAllocInfo == null) {
            try {
                CountDownLatch newLatch = new CountDownLatch(COUNTDOWN_NUMBER);
                Pair<DeviceSelector, String> waitingClient = new Pair<DeviceSelector, String>(deviceSelector, clientId);
                waitingClients.add(waitingClient);
                clientIdForLatch.put(clientId, newLatch);

                LOGGER.info("Client with id " + clientId + " waitnig for a device." + " Size: " + waitingClients.size());
                newLatch.await(waitForDeviceTimeout, TimeUnit.MILLISECONDS);

                clientIdForLatch.remove(clientId);
                waitingClients.remove(waitingClient);
                LOGGER.info("Client with id " + clientId + " removed from the queue.");
            } catch (InterruptedException e) {
                LOGGER.error("Waiting for an available device is interrupted.", e);
            }

            return clientIdForDeviceAllocationInformation.remove(clientId);
        }

        return dAllocInfo;
    }

    /**
     * Checks whether a client {@link DeviceSelector selector} is applicable to the newly available device(published or
     * released).
     *
     * @param deviceInformation
     *        - information about the {@link IDevice device}
     */
    private void checkClients(DeviceInformation deviceInformation) {
        synchronized (waitingClients) {
            availableDeviceLatch = new CountDownLatch(COUNTDOWN_NUMBER);

            ListIterator<Pair<DeviceSelector, String>> li = waitingClients.listIterator(waitingClients.size());
            while (li.hasPrevious()) {
                Pair<DeviceSelector, String> clientInfo = li.previous();
                DeviceSelector deviceSelector = clientInfo.getKey();
                String clientId = clientInfo.getValue();

                boolean isApplicable = slectorResolver.isApplicable(deviceSelector, deviceInformation);

                if (isApplicable) {
                    DeviceAllocationInformation dAlloc = allocate(deviceSelector);

                    if (dAlloc != null) {
                        li.remove();
                        clientIdForDeviceAllocationInformation.put(clientId, dAlloc);
                        clientIdForLatch.get(clientId).countDown();
                        break;
                    }
                }
            }

            availableDeviceLatch.countDown();
        }
    }

    private synchronized DeviceAllocationInformation allocate(DeviceSelector deviceSelector)
        throws NoDeviceMatchingTheGivenSelectorException,
            NoAvailableDeviceFoundException {
        List<IDevice> availableDevicesList = new ArrayList<>();

        try {
            devicePoolDao = PoolManager.getInstance().getDevicePoolDao();
            availableDevicesList = devicePoolDao.getDevices(deviceSelector, false);
            if (availableDevicesList.isEmpty()) {
                List<IDevice> notAvailableDeviceList = devicePoolDao.getDevices(deviceSelector, true);

                if (notAvailableDeviceList.isEmpty()) {
                    throw new NoDeviceMatchingTheGivenSelectorException();
                }
                return null;
            }
        } catch (DevicePoolDaoException e) {
            throw new NoDeviceMatchingTheGivenSelectorException();
        }

        IDevice device = availableDevicesList.get(0);

        DeviceInformation deviceInformation = device.getInformation();
        device.allocate();

        try {
            devicePoolDao.update(device);
        } catch (DevicePoolDaoException e) {
            String message = String.format("Allocating device with serial number %s failed.",
                                           deviceInformation.getSerialNumber());
            LOGGER.error(message, e);
        }

        final String bestMatchDeviceId = device.getDeviceId();
        long devicePasskey = device.getPasskey();
        DeviceAllocationInformation allocatedDeviceDescriptor = new DeviceAllocationInformation(devicePasskey,
                                                                                                bestMatchDeviceId);
        ClientRequestMonitor deviceMonitor = new ClientRequestMonitor();
        deviceMonitor.restartTimerForDevice(bestMatchDeviceId);

        return allocatedDeviceDescriptor;
    }

    /**
     * Called when device is published and is available for use.
     *
     * @param event
     *        - {@link DevicePublishedEvent event} that contains an information about published device
     */
    public void inform(DevicePublishedEvent event) {
        checkClients(event.getDeviceInformation());
    }

    /**
     * Called when device is released and is available for use again.
     *
     * @param event
     *        - {@link DeviceUnpublishedEvent event} that contains an information about released device.
     */
    public void inform(DeviceReleasedEvent event) {
        checkClients(event.getDeviceInformation());
    }

}
