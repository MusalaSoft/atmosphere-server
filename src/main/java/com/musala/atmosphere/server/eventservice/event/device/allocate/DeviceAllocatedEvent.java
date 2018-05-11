// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
