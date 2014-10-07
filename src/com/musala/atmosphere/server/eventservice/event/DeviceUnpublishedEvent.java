package com.musala.atmosphere.server.eventservice.event;

import com.musala.atmosphere.server.DeviceProxy;

/**
 * This event is published when a device is unpublished from the Server.
 * 
 * @author yavor.stankov
 * 
 */
public class DeviceUnpublishedEvent {
    private String onAgentId;

    private DeviceProxy deviceProxy;

    /**
     * Creates new event, which is holding information about the unpublished device.
     * 
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     * @param deviceProxy
     *        - the remote device object, that has been unpublished from the server
     */
    public DeviceUnpublishedEvent(String onAgentId, DeviceProxy deviceProxy) {
        this.onAgentId = onAgentId;
        this.deviceProxy = deviceProxy;
    }

    /**
     * Gets the remote device object, that has been unpublished from the server.
     * 
     * @return remote device object
     */
    public DeviceProxy getUnpublishedDeviceProxy() {
        return deviceProxy;
    }

    /**
     * Gets the identifier of the agent on which the device is registered.
     * 
     * @return the identifier of the agent
     */
    public String getUnpublishDeviceOnAgentId() {
        return onAgentId;
    }

}
