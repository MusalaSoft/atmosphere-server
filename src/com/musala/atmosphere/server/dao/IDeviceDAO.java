package com.musala.atmosphere.server.dao;

import com.musala.atmosphere.commons.DeviceInformation;

/**
 * A data access object to manipulate a device entry in a data source.
 * 
 * @author delyan.dimitrov
 * 
 */
public interface IDeviceDAO {
    /**
     * Marks the device as allocated in the data source.
     * 
     * @return <code>true</code> if the allocation is successful, and <code>false</code> false otherwise
     */
    public boolean allocate();

    /**
     * Marks the device as released in the data source.
     * 
     * @return <code>true</code> if the device was successfully released, and <code>false</code> false otherwise
     */
    public boolean release();

    /**
     * Gets the information of the device from the data source.
     * 
     * @return the {@link DeviceInformation device information} of this device or <code>null</code> if the operation
     *         fails
     */
    public DeviceInformation getInformation();

    /**
     * Gets the ID of the device.
     * 
     * @return the ID of the device
     */
    public String getId();

    /**
     * Checks whether the device is allocated.
     * 
     * @return <code>true</code> if the device is allocated, and <code>false</code> otherwise
     */
    public boolean isAllocated();
}
