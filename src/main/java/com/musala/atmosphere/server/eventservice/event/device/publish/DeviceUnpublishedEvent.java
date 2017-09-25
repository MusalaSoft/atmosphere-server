package com.musala.atmosphere.server.eventservice.event.device.publish;

/**
 * This event is published when a device is unpublished from the Server.
 *
 * @author yavor.stankov
 *
 */
public class DeviceUnpublishedEvent implements DevicePublishEvent {
    private String onAgentId;

    private String deviceSerialNumber;

    /**
     * Creates new event, which is holding information about the unpublished device.
     *
     * @param deviceSerialNumber
     *        - the serial number of the device, that has been unpublished from the server
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     */
    public DeviceUnpublishedEvent(String deviceSerialNumber, String onAgentId) {
        this.onAgentId = onAgentId;
        this.deviceSerialNumber = deviceSerialNumber;
    }

    /**
     * Gets the identifier of the agent on which the device is registered.
     *
     * @return the identifier of the agent
     */
    public String getUnpublishDeviceOnAgentId() {
        return onAgentId;
    }

    /**
     * Gets the serial number of the un-registered device.
     *
     * @return serial number of the device
     */
    public String getUnpublishedDeviceSerialNumber() {
        return deviceSerialNumber;
    }
}
