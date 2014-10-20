package com.musala.atmosphere.server.registrymanager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishedEvent;

/**
 * 
 * @author yavor.stankov
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteObjectRegistryManagerTest {
    private static String onAgentId = "123";

    private static String deviceSerialNumber = "112233";

    private static String deviceUniqueIdentifier = "123_112233";

    private DeviceProxy deviceProxy = mock(DeviceProxy.class);

    public RemoteObjectRegistryManager rmiManager;

    private Registry serverRmiRegistry;

    private DevicePublishedEvent devicePublishedEvent;

    @Before
    public void setup() {
        serverRmiRegistry = mock(Registry.class);
        rmiManager = new RemoteObjectRegistryManager(serverRmiRegistry);

        devicePublishedEvent = new DevicePublishedEvent(deviceProxy, deviceSerialNumber, onAgentId);
    }

    @Test
    public void testInformDevicePublish() throws RemoteException {
        rmiManager.inform(devicePublishedEvent);

        verify(serverRmiRegistry).rebind(deviceUniqueIdentifier, deviceProxy);
    }
}
