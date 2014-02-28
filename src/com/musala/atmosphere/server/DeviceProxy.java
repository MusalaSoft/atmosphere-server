package com.musala.atmosphere.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.cs.InvalidPasskeyException;
import com.musala.atmosphere.commons.cs.clientdevice.IClientDevice;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;

/**
 * The DeviceProxy object is used in RMI. It reroutes invocations of it's methods (by the {@link IClientDevice
 * IClientDevice} stub) to invocations on another RMI stub (the {@link IWrapDevice IWrapDevice}).
 * 
 * @author georgi.gaydarov
 * 
 */
public class DeviceProxy extends UnicastRemoteObject implements IClientDevice {
    private static final long serialVersionUID = -2645952387036199816L;

    private final IWrapDevice wrappedDevice;

    private DeviceInformation deviceInformation;

    private PasskeyAuthority passkeyAuthority;

    private ClientRequestMonitor timeoutMonitor = ClientRequestMonitor.getInstance();

    public DeviceProxy(IWrapDevice deviceToWrap) throws RemoteException {
        wrappedDevice = deviceToWrap;
        passkeyAuthority = PasskeyAuthority.getInstance();
    }

    @Override
    public Object route(long invocationPasskey, RoutingAction action, Object... args)
        throws RemoteException,
            CommandFailedException,
            InvalidPasskeyException {
        passkeyAuthority.validatePasskey(this, invocationPasskey);
        timeoutMonitor.restartTimerForDevice(this);
        try {
            Object returnValue = wrappedDevice.route(action, args);
            return returnValue;
        } catch (RemoteException e) {
            // TODO handle remote exception (the server should know the connection is bad)
            // and decide what to do next. This next line is temporal.
            throw new RuntimeException("Connection to device failed.", e);
        }
    }

}
