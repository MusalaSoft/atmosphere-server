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

package com.musala.atmosphere.server.dao;

import java.util.List;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;

/**
 * A data access object for using device information in a data source.
 *
 * @author delyan.dimitrov
 *
 */
public interface IDevicePoolDao {
    /**
     * Adds a device entry in the data source.
     *
     * @param device
     *        - the information about the device
     * @param deviceId
     *        - the ID of the device
     * @param agentId
     *        - the ID of the agent that the device is connected to
     * @param passkey
     *        - passkey for validating authority
     * @return a {@link IDevice data access object} for interacting with the newly created device entry in the data
     *         source or throws {@link DevicePoolDaoException exception} if adding fails
     * @throws DevicePoolDaoException
     *         - thrown when adding device fails
     */
    public IDevice addDevice(DeviceInformation device, String deviceId, String agentId, long passkey)
        throws DevicePoolDaoException;

    /**
     * Gets a device by its ID.
     *
     * @param id
     *        - the ID of the requested device
     * @return the {@link IDevice device} with the given ID or or throws {@link DevicePoolDaoException exception} if
     *         getting fails
     * @throws DevicePoolDaoException
     *         - thrown when getting device with the requested ID fails
     */
    public IDevice getDevice(String id) throws DevicePoolDaoException;

    /**
     * Checks if a device with such ID is available in the data source.
     *
     * @param id
     *        - the ID to be checked
     * @return <code>true</code> if a device with such ID exists, and <code>false</code> otherwise
     */
    public boolean hasDevice(String id);

    /**
     * Removes the devices connected to the agent with the given ID from the data source.
     *
     * @param agentId
     *        - the ID of the agent whose devices should be removed
     * @return the number of the devices which are removed successfully
     * @throws DevicePoolDaoException
     *         - thrown when removing devices on the agent with the given ID fails
     */
    public int removeDevices(String agentId) throws DevicePoolDaoException;

    /**
     * Removes the device with the given ID from the data source.
     *
     * @param deviceId
     *        - the ID of the device that should be removed
     * @throws DevicePoolDaoException
     *         - thrown when removing device with the given ID fails
     */
    public void remove(String deviceId) throws DevicePoolDaoException;

    /**
     * Updates the given {@link IDevice device} the device pool.
     *
     * @param device
     *        - device that will be updated in pool
     * @throws DevicePoolDaoException
     *         - thrown when updating device fails
     *
     */
    public void update(IDevice device) throws DevicePoolDaoException;

    /**
     * Gets all devices that match the given selector and allocation criterion.
     *
     * @param selector
     *        - contains all the characteristics that the requested devices should match
     * @param isAllocated
     *        - if <code>true</code> only allocated devices are filtered, otherwise devices are selected from the free
     *        ones
     * @return a {@link List list} of {@link IDevice devices} that match the passed parameters
     * @throws DevicePoolDaoException
     *         when getting devices with the requested selector fails
     */
    public List<IDevice> getDevices(DeviceSelector selector, boolean isAllocated) throws DevicePoolDaoException;

    /**
     * Checks if the data source contains a device that matches the given selector and allocation criterion.
     *
     * @param selector
     *        - contains all the characteristics that the requested devices should match
     * @param isAllocated
     *        - if <code>true</code> only allocated devices are filtered, otherwise devices are selected from the free
     *        ones
     * @throws DevicePoolDaoException
     *         - this exception is thrown when operations with the data access object for the device pool in the data
     *         source fail.
     * @return <code>true</code> if device matching the given selector exists, and <code>false</code> otherwise
     */
    public boolean hasDevice(DeviceSelector selector, boolean isAllocated) throws DevicePoolDaoException;
}
