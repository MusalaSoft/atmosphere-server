package com.musala.atmosphere.server.eventservice.event.device.allocate;

import com.musala.atmosphere.commons.DeviceInformation;

/**
 * This event is published when a device is released.
 *
 * @author yavor.stankov
 *
 */
public class DeviceReleasedEvent implements DeviceAllocateEvent {
    private DeviceInformation deviceInformation;

    public DeviceReleasedEvent(DeviceInformation deviceInformation) {
        this.deviceInformation = deviceInformation;
    }

    public DeviceInformation getDeviceInformation() {
        return deviceInformation;
    }
}
