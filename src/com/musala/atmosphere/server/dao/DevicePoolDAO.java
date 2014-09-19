package com.musala.atmosphere.server.dao;

import java.util.List;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;

/**
 * A data access object for using device information in a data source.
 * 
 * @author delyan.dimitrov
 * 
 */
public interface DevicePoolDAO {
    /**
     * Adds a device entry in the data source.
     * 
     * @param device
     *        - the information about the device
     * @param rmiId
     *        - the ID of the device in the RMI registry
     * @param agentId
     *        - the ID of the agent that the device is connected to
     * @return a {@link DeviceDAO data access object} for interacting with the newly created device entry in the data
     *         source or <code>null</code> if the insertion fails
     */
    public DeviceDAO addDevice(DeviceInformation device, String rmiId, String agentId);

    /**
     * Gets all devices that match the given parameters.
     * 
     * @param parameters
     *        - the parameters that the requested devices should match
     * @return a {@link List list} of {@link DeviceDAO devices} that match the passed parameters
     */
    public List<DeviceDAO> getDevices(DeviceParameters parameters);

    /**
     * Gets a device by its ID.
     * 
     * @param id
     *        - the ID of the requested device
     * @return the {@link DeviceDAO device} with the given ID or <code>null</code> if no such device exists
     */
    public DeviceDAO getDevice(String id);

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
     * @return <code>true</code> if the removal is successful, and <code>false</code> false otherwise
     */
    public boolean removeDeivces(String agentId);

    /**
     * Removes a device with the given ID from the data source.
     * 
     * @param id
     *        - the ID of the device to be removed
     * @return <code>true</code> if the removal is successful, and <code>false</code> false otherwise
     */
    public boolean remove(String id);
}
