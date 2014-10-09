package com.musala.atmosphere.server.registrymanager;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.eventservice.event.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.DeviceUnpublishedEvent;

/**
 * 
 * @author yavor.stankov
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteObjectRegistryManagerTest {
    private static String deviceProxyRmiId = "123";

    private static String onAgentId = "123";

    private static String deviceSerialNumber = "112233";

    private static String deviceUniqueIdentifier = "123_112233";

    private DeviceProxy deviceProxy = mock(DeviceProxy.class);

    public RemoteObjectRegistryManager rmiManager;

    private Registry serverRmiRegistry;

    private DevicePublishedEvent devicePublishedEvent;

    private DeviceUnpublishedEvent deviceUnpublishedEvent;

    @Before
    public void setup() {
        serverRmiRegistry = mock(Registry.class);
        rmiManager = new RemoteObjectRegistryManager(serverRmiRegistry);

        devicePublishedEvent = new DevicePublishedEvent(deviceProxy, deviceSerialNumber, onAgentId);
        deviceUnpublishedEvent = new DeviceUnpublishedEvent(deviceProxy, deviceSerialNumber, onAgentId);
    }

    @Test
    public void testPublishObject() throws RemoteException {
        rmiManager.publishObject(deviceProxy, deviceProxyRmiId);
        verify(serverRmiRegistry).rebind(onAgentId, deviceProxy);
    }

    @Test(expected = RemoteException.class)
    public void testPublishObjectThrowsException() throws RemoteException {
        doThrow(new RemoteException()).when(serverRmiRegistry).rebind(onAgentId, deviceProxy);

        rmiManager.publishObject(deviceProxy, deviceProxyRmiId);
    }

    @Test
    public void testUnpublishObject() throws RemoteException, NotBoundException {
        rmiManager.publishObject(deviceProxy, deviceProxyRmiId);

        rmiManager.unpublishObject(deviceProxyRmiId);
        verify(serverRmiRegistry).unbind(deviceProxyRmiId);
    }

    @Test(expected = RemoteException.class)
    public void testUnpublishObjectThrowsException() throws RemoteException, NotBoundException {
        rmiManager.publishObject(deviceProxy, deviceProxyRmiId);

        doThrow(new RemoteException()).when(serverRmiRegistry).unbind(deviceProxyRmiId);

        rmiManager.unpublishObject(deviceProxyRmiId);
    }

    @Test
    public void testInformDevicePublish() throws RemoteException {
        rmiManager.inform(devicePublishedEvent);

        verify(serverRmiRegistry).rebind(deviceUniqueIdentifier, deviceProxy);
    }

    @Test
    public void testInformDeviceUnpublished() throws RemoteException, NotBoundException {
        rmiManager.inform(devicePublishedEvent);

        rmiManager.inform(deviceUnpublishedEvent);

        verify(serverRmiRegistry).unbind(deviceUniqueIdentifier);
    }
}
