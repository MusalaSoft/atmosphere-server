package com.musala.atmosphere.server.dao;

import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.data.model.IDevice;

/**
 * A data access object to manipulate a device entry in a data source.
 * 
 * @author delyan.dimitrov
 * 
 */
public interface IDeviceDao {
    /**
     * Updates device properties in the data source.
     * 
     * @param device
     *        - device that will be updated in the data source
     * @throws DeviceDaoException
     *         - thrown when updating device fails
     * 
     */
    public void update(IDevice device) throws DeviceDaoException;

    /**
     * Adds new device in the data source.
     * 
     * @param device
     *        - device to be added in the data source
     * @throws DeviceDaoException
     *         - thrown when adding new device fails
     */
    public void add(IDevice device) throws DeviceDaoException;

    /**
     * Removes a device with the given ID from the data source.
     * 
     * @param deviceId
     *        - the ID of the device to be removed
     * @throws DeviceDaoException
     *         - thrown when removing device fails
     */
    public void remove(String deviceId) throws DeviceDaoException;
}
