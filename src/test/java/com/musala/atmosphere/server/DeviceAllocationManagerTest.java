package com.musala.atmosphere.server;

import static org.awaitility.Awaitility.await;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceAllocationInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Tests the {@link DeviceAllocationManager allocation manager class}.
 * 
 * @author dimcho.nedev
 *
 */
public class DeviceAllocationManagerTest {
    private static final String AGENT_ID = "agent_id";

    private static final int START_SERVER_TIMEOUT = 2000;

    private static final String[] DEVICE_MODELS = {"Pixel XL", "Nexus 5X", "Nexus 5", "Nexus 4"};

    private static final int WAIT_FOR_OTHER_CLIENTS_TIMEOUT = 10_000;

    private static final String MESSAGE_FORMAT = "The expected number of the %s clients are different from the actual.";

    private static final String UNEXPECTED_SERVED_NUMBER_MESSAGE = String.format(MESSAGE_FORMAT, "served");

    private static final String UNEXPECTED_UNSERVED_NUMBER_MESSAGE = String.format(MESSAGE_FORMAT, "unserved");

    private static PoolManager poolManager;

    private static DeviceAllocationManager dAllocManager;

    private List<String> servedClientsList = Collections.synchronizedList((new ArrayList<>()));

    private List<String> unservedClientsList = Collections.synchronizedList((new ArrayList<>()));

    private static List<Pair<DeviceSelector, String>> waitingClients;

    private static Server server;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUp() throws InterruptedException {
        try {
            server = new Server();
            server.run();
            Thread.sleep(START_SERVER_TIMEOUT);

            ServerManager serverManager = getFieldObject(server, "serverManager");
            dAllocManager = (DeviceAllocationManager) getFieldObject(server, "allocationManager");

            serverManager.registerAgent(AGENT_ID);

            poolManager = getFieldObject(serverManager, "poolManager");
            waitingClients = (List<Pair<DeviceSelector, String>>) getFieldObject(dAllocManager, "waitingClients");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void before() {
        try {
            poolManager.removeAllDevices();
        } catch (DevicePoolDaoException | CommandFailedException e) {
            e.printStackTrace();
        }
        servedClientsList.clear();
        unservedClientsList.clear();
    }

    @AfterClass
    public static void cleanUp() {
        server.exit();
    }

    @Test
    public void allClientsShouldBeServedTest() {
        final int expectedServedClientsCount = 10;
        for (int i = 0; i < expectedServedClientsCount; i++) {
            poolManager.addDevice(generateDeviceInformation("d" + i, DEVICE_MODELS[0], 25), AGENT_ID);
        }

        DeviceSelector deviceSelector = new DeviceSelectorBuilder().targetApi(25)
                                                                   .deviceType(DeviceType.DEVICE_PREFERRED)
                                                                   .build();
        generateConcurrentClients(deviceSelector, expectedServedClientsCount, 500);

        await().atMost(WAIT_FOR_OTHER_CLIENTS_TIMEOUT, TimeUnit.MILLISECONDS)
               .until(() -> waitingClients.size() == 0 && servedClientsList.size() == expectedServedClientsCount);

        Assert.assertEquals(0, waitingClients.size());
        Assert.assertEquals(expectedServedClientsCount, servedClientsList.size());
    }

    @Test
    public void largeNumberOfUnservedClientsTest() throws InterruptedException {
        final int clientsCount = 100;
        final int expectedServedClientsCount = 1;
        int expectedUnservedClientsCount = clientsCount - expectedServedClientsCount;

        poolManager.addDevice(generateDeviceInformation("d1", DEVICE_MODELS[0], 25), AGENT_ID);
        DeviceSelector deviceSelector = new DeviceSelectorBuilder().targetApi(25)
                                                                   .deviceType(DeviceType.DEVICE_PREFERRED)
                                                                   .build();
        generateConcurrentClients(deviceSelector, clientsCount, 250);

        await().atMost(WAIT_FOR_OTHER_CLIENTS_TIMEOUT, TimeUnit.MILLISECONDS)
               .until(() -> waitingClients.size() == 0 && servedClientsList.size() == expectedServedClientsCount
                       && unservedClientsList.size() == expectedUnservedClientsCount);

        Assert.assertEquals(0, waitingClients.size());
        Assert.assertEquals(UNEXPECTED_SERVED_NUMBER_MESSAGE, expectedServedClientsCount, servedClientsList.size());
        Assert.assertEquals(UNEXPECTED_UNSERVED_NUMBER_MESSAGE, expectedUnservedClientsCount, unservedClientsList.size());
    }

    @Test
    public void variousDeviceAllocationsTest() throws DevicePoolDaoException, InterruptedException {
        poolManager.addDevice(generateDeviceInformation("d1", DEVICE_MODELS[0], 25), AGENT_ID);
        poolManager.addDevice(generateDeviceInformation("d2", DEVICE_MODELS[1], 25), AGENT_ID);
        poolManager.addDevice(generateDeviceInformation("d3", DEVICE_MODELS[2], 23), AGENT_ID);
        poolManager.addDevice(generateDeviceInformation("d4", DEVICE_MODELS[3], 22), AGENT_ID);

        int clientsCount = 10;
        int expectedServedClients = 2;
        int expectedWaitingClients = clientsCount - expectedServedClients;

        DeviceSelector deviceSelector = new DeviceSelectorBuilder().targetApi(25)
                                                                   .deviceType(DeviceType.DEVICE_PREFERRED)
                                                                   .build();

        generateConcurrentClients(deviceSelector, clientsCount, 5_000);

        await().atMost(WAIT_FOR_OTHER_CLIENTS_TIMEOUT, TimeUnit.MILLISECONDS)
               .until(() -> waitingClients.size() == expectedWaitingClients
                       && servedClientsList.size() == expectedServedClients);

        Assert.assertEquals(expectedServedClients, servedClientsList.size());
        Assert.assertEquals(expectedWaitingClients, waitingClients.size());

        // should get a device immediately
        deviceSelector = new DeviceSelectorBuilder().minApi(19).deviceType(DeviceType.DEVICE_PREFERRED).build();

        // Add a device that does not satisfy none of the selectors
        DeviceAllocationInformation dAllocInfo = dAllocManager.allocateDevice(deviceSelector, "client_11", 1_000);
        Assert.assertNotNull(dAllocInfo);

        Assert.assertEquals("The waiting clients count should not be changed.",
                            expectedWaitingClients,
                            waitingClients.size());

        // generate events
        poolManager.releaseDevice(AGENT_ID + "_d1"); // device released
        poolManager.releaseDevice(AGENT_ID + "_d2"); // device released
        poolManager.addDevice(generateDeviceInformation("d5", DEVICE_MODELS[0], 25), AGENT_ID); // device published

        await().atMost(WAIT_FOR_OTHER_CLIENTS_TIMEOUT, TimeUnit.MILLISECONDS)
               .until(() -> servedClientsList.size() == 5 && unservedClientsList.size() == 5);

        Assert.assertEquals(0, waitingClients.size());
        Assert.assertEquals(UNEXPECTED_SERVED_NUMBER_MESSAGE, 5, servedClientsList.size());
        Assert.assertEquals(UNEXPECTED_UNSERVED_NUMBER_MESSAGE, 5, unservedClientsList.size());
    }

    private void generateConcurrentClients(DeviceSelector deviceSelector,
                                           final int numberOfClients,
                                           final int clientTimout) {
        for (int index = 0; index < numberOfClients; index++) {
            new Thread(new Runnable() {
                private int index;

                public Runnable init(int index) {
                    this.index = index;
                    return this;
                }

                @Override
                public void run() {
                    String clientId = "client_" + index;

                    DeviceAllocationInformation dAllocInfo = dAllocManager.allocateDevice(deviceSelector,
                                                                                          clientId,
                                                                                          clientTimout);
                    if (dAllocInfo != null) {
                        System.out.println(dAllocInfo.getDeviceId());
                        servedClientsList.add(clientId);
                    } else {
                        unservedClientsList.add(clientId);
                    }
                }
            }.init(index)).start();
        }
    }

    private static final DeviceInformation generateDeviceInformation(String serial, String model, int apiLevel) {
        DeviceInformation dInfo = new DeviceInformation();
        dInfo.setSerialNumber(serial);
        dInfo.setApiLevel(apiLevel);
        dInfo.setEmulator(false);
        dInfo.setModel(model);
        dInfo.setEmulator(false);

        return dInfo;
    }

    @SuppressWarnings("unchecked")
    private static <T, K> T getFieldObject(K parantObject, String childFieldName)
        throws NoSuchFieldException,
            SecurityException,
            IllegalArgumentException,
            IllegalAccessException {
        Class<?> clazz = parantObject.getClass();
        Field field = clazz.getDeclaredField(childFieldName);
        field.setAccessible(true);

        return (T) field.get(parantObject);
    }

}
