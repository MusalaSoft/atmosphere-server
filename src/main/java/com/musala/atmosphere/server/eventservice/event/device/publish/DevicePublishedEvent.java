package com.musala.atmosphere.server.eventservice.event.device.publish;

import com.musala.atmosphere.commons.DeviceInformation;

/**
 * This event is published when a device is published to the Server.
 *
 * @author yavor.stankov
 *
 */
public class DevicePublishedEvent implements DevicePublishEvent {

    private String onAgentId;

    private DeviceInformation deviceInformation;

    /**
     * Creates new event, which is holding information about the published device.
     *
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     * @param deviceInformation
     *        - {@link DeviceInformation information} about the published device
     */
    public DevicePublishedEvent(String onAgentId, DeviceInformation deviceInformation) {
        this.onAgentId = onAgentId;
        this.deviceInformation = deviceInformation;
    }

    /**
     * Gets the identifier of the agent on which the device is registered.
     *
     * @return identifier of the agent
     */
    public String getAgentId() {
        return onAgentId;
    }

    /**
     * Gets {@link DeviceInformation information} about the published device.
     *
     * @return {@link DeviceInformation device information}
     */
    public DeviceInformation getDeviceInformation() {
        return deviceInformation;
    }
}
