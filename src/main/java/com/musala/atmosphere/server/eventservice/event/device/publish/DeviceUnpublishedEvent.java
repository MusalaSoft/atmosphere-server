package com.musala.atmosphere.server.eventservice.event.device.publish;

import com.musala.atmosphere.server.DeviceProxy;

/**
 * This event is published when a device is unpublished from the Server.
 * 
 * @author yavor.stankov
 * 
 */
public class DeviceUnpublishedEvent implements DevicePublishEvent {
    private String onAgentId;

    private DeviceProxy deviceProxy;

    private String deviceSerialNumber;

    /**
     * Creates new event, which is holding information about the unpublished device.
     * 
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     * @param deviceSerialNumber
     *        - the serial number of the device, that has been unpublished from the server
     * @param deviceProxy
     *        - the remote device object, that has been unpublished from the server
     */
    public DeviceUnpublishedEvent(DeviceProxy deviceProxy, String deviceSerialNumber, String onAgentId) {
        this.onAgentId = onAgentId;
        this.deviceProxy = deviceProxy;
        this.deviceSerialNumber = deviceSerialNumber;
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

    /**
     * Gets the serial number of the un-registered device.
     * 
     * @return serial number of the device
     */
    public String getUnpublishedDeviceSerialNumber() {
        return deviceSerialNumber;
    }
}
