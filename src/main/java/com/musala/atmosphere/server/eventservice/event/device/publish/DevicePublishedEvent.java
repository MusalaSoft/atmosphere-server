package com.musala.atmosphere.server.eventservice.event.device.publish;

/**
 * This event is published when a device is published to the Server.
 *
 * @author yavor.stankov
 *
 */
public class DevicePublishedEvent implements DevicePublishEvent {

    private String onAgentId;

    private String deviceSerialNumber;

    /**
     * Creates new event, which is holding information about the published device.
     *
     * @param deviceProxy
     *        - the remote device object, that has been published to the server
     * @param deviceSerialNumber
     *        - the serial number of the device, that has been published to the server
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     */
    public DevicePublishedEvent(String deviceSerialNumber, String onAgentId) {
        this.onAgentId = onAgentId;
        this.deviceSerialNumber = deviceSerialNumber;
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
     * Gets the serial number of the registered device.
     *
     * @return serial number of the device
     */
    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }
}
