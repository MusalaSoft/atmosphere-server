package com.musala.atmosphere.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.cs.clientdevice.IClientDevice;
import com.musala.atmosphere.commons.cs.exception.DeviceNotFoundException;
import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;
import com.musala.atmosphere.server.websocket.ServerDispatcher;

/**
 * The DeviceProxy object is used in RMI. It reroutes invocations of it's methods (by the {@link IClientDevice
 * IClientDevice} stub) to invocations on another RMI stub (the {@link IWrapDevice IWrapDevice}).
 *
 * @author georgi.gaydarov
 *
 */
public class DeviceProxy extends UnicastRemoteObject implements IClientDevice {
    private final static Logger LOGGER = Logger.getLogger(DeviceProxy.class);

    private static final long serialVersionUID = -2645952387036199816L;

    private ClientRequestMonitor timeoutMonitor = new ClientRequestMonitor();

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    private String deviceId;

    public DeviceProxy(String deviceId) throws RemoteException {
        this.deviceId = deviceId;
    }

    @Override
    public Object route(long invocationPasskey, RoutingAction action, Object... args)
            throws RemoteException,
            CommandFailedException,
            InvalidPasskeyException,
            DeviceNotFoundException,
            RuntimeException {
        PasskeyAuthority.validatePasskey(invocationPasskey, deviceId);

        timeoutMonitor.restartTimerForDevice(deviceId);

        return dispatcher.executeRoute(deviceId, action, args);
    }
    
}
