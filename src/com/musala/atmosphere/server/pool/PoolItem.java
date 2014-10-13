package com.musala.atmosphere.server.pool;

import java.rmi.RemoteException;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.server.DeviceProxy;
import com.musala.atmosphere.server.ServerManager;

/**
 * The {@link PoolItem PoolItem} object is a managing container for a {@link DeviceProxy DeviceProxy} instance. It is
 * used by the {@link ServerManager PoolManager} as a pooling object.
 * 
 * @author georgi.gaydarov
 * 
 */
public class PoolItem {
    private DeviceProxy deviceProxy;

    private DeviceInformation deviceInformation;

    private boolean availability = true; // Device is available on creation.

    private final String onAgentId;

    /**
     * Creates a new {@link PoolItem PoolItem} object that wraps a device wrapper in a {@link DeviceProxy DeviceProxy}
     * object and publishes it on the server's RMI registry.
     * 
     * @param deviceWrapperId
     *        - RMI string identifier on the Agent for the device wrapper stub.
     * @param deviceWrapper
     *        - device to be wrapped in a {@link DeviceProxy DeviceProxy} object.
     * @param agentManager
     *        - the {@link AgentManager AgentManager} that published the {@link IWrapDevice IWrapDevice} we are
     *        wrapping.
     * @param serverRmiRegistry
     *        - RMI registry in which we will publish the newly created {@link DeviceProxy DeviceProxy} wrapper.
     * @throws RemoteException
     */
    public PoolItem(DeviceProxy deviceProxy, DeviceInformation deviceInformation, String onAgentId)
        throws RemoteException,
            CommandFailedException {
        this.deviceProxy = deviceProxy;
        this.deviceInformation = deviceInformation;
        this.onAgentId = onAgentId;
    }

    public DeviceInformation getUnderlyingDeviceInformation() {
        return deviceInformation;
    }

    /**
     * Checks whether the current device is corresponding to an actual device on the agent.
     * 
     * @param agentId
     *        - the agent on which the real device is plugged.
     * @param deviceProxyId
     *        - the proxy id of the device connected on the agent.
     * @return True if the device actually exists on the agent, false if not.
     */
    public boolean isCorrespondingTo(String agentId) {
        return agentId.equals(onAgentId);
    }

    /**
     * Check whether a device is available for allocation to a Client.
     * 
     * @return - True if the device is available in the pool, false if the device is already allocated.
     */
    public boolean isAvailable() {
        return availability;
    }

    void setAvailability(boolean availability) {
        this.availability = availability;
    }

    /**
     * Returns the underlying {@link DeviceProxy DeviceProxy} instance that is responsible for redirecting request.
     * 
     * @return the {@link DeviceProxy DeviceProxy} instance.
     */
    public DeviceProxy getUnderlyingDeviceProxy() {
        return deviceProxy;
    }

    public String getUnderlyingAgentId() {
        return onAgentId;
    }

}
