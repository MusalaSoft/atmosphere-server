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

package com.musala.atmosphere.server.data.model;

import com.musala.atmosphere.commons.DeviceInformation;

public interface IDevice {
    /**
     * Marks the device as allocated in the data source. Device in the data source needs to be updated to take effect.
     */
    public void allocate();

    /**
     * Marks the device as released. Device in the data source needs to be updated to take effect.
     */
    public void release();

    /**
     * Checks whether the device is allocated.
     *
     * @return <code>true</code> if the device is allocated, and <code>false</code> otherwise
     */
    public boolean isAllocated();

    /**
     * Gets the information of the device in the data source.
     *
     * @return {@link DeviceInformation information} about the device
     */
    public DeviceInformation getInformation();

    /**
     * Gets the ID of the agent responsible for this device.
     *
     * @return ID of the agent on which the device is running or <code>null</code> if the agent is not set
     */
    public String getAgentId();

    /**
     * Gets the ID for this device.
     *
     * @return ID of the device
     */
    public String getDeviceId();

    /**
     * Gets the passkey for this device.
     *
     * @return passkey of the device
     */
    public long getPasskey();

    /**
     * Sets the passkey for this device.
     *
     * @param passkey
     *        - the passkey for this device
     */
    public void setPasskey(long passkey);

    /**
     * Sets the information of the device in the data source.
     *
     * @param information
     *        - the device {@link DeviceInformation information} to be set
     */
    public void setDeviceInformation(DeviceInformation information);
}
