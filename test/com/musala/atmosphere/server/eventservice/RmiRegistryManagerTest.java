package com.musala.atmosphere.server.eventservice;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.server.DeviceProxy;

/**
 * 
 * @author yavor.stankov
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class RmiRegistryManagerTest {
    private static String deviceWrapperAgentRmiId = "123";

    private static String onAgentId = "321";

    private DeviceProxy deviceProxy = mock(DeviceProxy.class);

    public RmiRegistryManager rmiManager;

    private Registry serverRmiRegistry;

    private String deviceProxyRmiString = null;

    @Before
    public void setup() {
        serverRmiRegistry = mock(Registry.class);
        rmiManager = new RmiRegistryManager(serverRmiRegistry);

        deviceProxyRmiString = rmiManager.buildDeviceRmiIdentifier(onAgentId, deviceWrapperAgentRmiId);
    }

    @Test
    public void testPublishDevice() throws RemoteException {
        rmiManager.publishDevice(deviceProxy, deviceWrapperAgentRmiId, onAgentId);
        verify(serverRmiRegistry).rebind(deviceProxyRmiString, deviceProxy);
    }

    @Test(expected = RemoteException.class)
    public void testPublishDeviceThrowsException() throws RemoteException {
        doThrow(new RemoteException()).when(serverRmiRegistry).rebind(deviceProxyRmiString, deviceProxy);

        rmiManager.publishDevice(deviceProxy, deviceWrapperAgentRmiId, onAgentId);
    }

}
