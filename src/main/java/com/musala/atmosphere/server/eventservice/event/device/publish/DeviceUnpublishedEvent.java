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
