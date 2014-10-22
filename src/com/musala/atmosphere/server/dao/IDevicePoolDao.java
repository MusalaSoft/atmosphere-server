package com.musala.atmosphere.server.dao;

import java.util.List;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
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
     * @param rmiId
     *        - the ID of the device in the RMI registry
     * @param agentId
     *        - the ID of the agent that the device is connected to
     * @return a {@link IDevice data access object} for interacting with the newly created device entry in the data
     *         source or throws {@link DevicePoolDaoException exception} if adding fails
     * @throws DevicePoolDaoException
     *         - thrown when adding device fails
     */
    public IDevice addDevice(DeviceInformation device, String rmiId, String agentId) throws DevicePoolDaoException;

    /**
     * Gets all devices that match the given parameters.
     * 
     * @param parameters
     *        - the parameters that the requested devices should match
     * @return a {@link List list} of {@link IDevice devices} that match the passed parameters or throws
     *         {@link DevicePoolDaoException exception} if the operation fails
     * @throws DevicePoolDaoException
     *         - thrown when getting devices with the requested {@link DeviceParameters parameters} fails
     */
    public List<IDevice> getDevices(DeviceParameters parameters) throws DevicePoolDaoException;

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
     * Checks if the data source contains a device that matches the given parameters.
     * 
     * @param parameters
     *        - the parameters to be checked
     * @return <code>true</code> if device matching the given parameters exists, and <code>false</code> otherwise
     */
    public boolean hasDevice(DeviceParameters parameters);

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
}
