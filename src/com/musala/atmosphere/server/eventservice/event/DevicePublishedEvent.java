package com.musala.atmosphere.server.eventservice.event;

import com.musala.atmosphere.server.DeviceProxy;

/**
 * This event is published when a device is published to the Server.
 * 
 * @author yavor.stankov
 * 
 */
public class DevicePublishedEvent {

    private DeviceProxy deviceProxy;

    private String onAgentId;

    /**
     * Creates new event, which is holding information about the published device.
     * 
     * @param deviceProxy
     *        - the remote device object, that has been published to the server
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     */
    public DevicePublishedEvent(DeviceProxy deviceProxy, String deviceWrapperAgentRmiId, String onAgentId) {
        this.deviceProxy = deviceProxy;
        this.onAgentId = onAgentId;
    }

    /**
     * Gets the remote device object, that has been published to the server.
     * 
     * @return remote device object
     */
    public DeviceProxy getDeviceProxy() {
        return deviceProxy;
    }

    /**
     * Gets the identifier of the agent on which the device is registered.
     * 
     * @return identifier of the agent
     */
    public String getAgentId() {
        return onAgentId;
    }

}
