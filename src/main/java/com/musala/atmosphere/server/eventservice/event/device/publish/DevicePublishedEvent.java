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
