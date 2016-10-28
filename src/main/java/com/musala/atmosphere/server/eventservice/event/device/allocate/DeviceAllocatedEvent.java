package com.musala.atmosphere.server.eventservice.event.device.allocate;

/**
 * This event is published when a device is allocated
 * 
 * @author yavor.stankov
 * 
 */
public class DeviceAllocatedEvent implements DeviceAllocateEvent {
    private String deviceId;

    /**
     * Creates new event, which is holding information about the allocated device.
     * 
     * @param deviceId
     *        - the device ID
     */
    public DeviceAllocatedEvent(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the device ID.
     * 
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }
}
