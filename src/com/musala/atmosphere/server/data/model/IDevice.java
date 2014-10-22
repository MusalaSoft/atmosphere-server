package com.musala.atmosphere.server.data.model;

import com.musala.atmosphere.commons.DeviceInformation;

public interface IDevice {
    /**
     * Marks the device as allocated in the data source.
     * 
     * @Note Device in the data source needs to be updated to take effect.
     */
    public void allocate();

    /**
     * Marks the device as released.
     * 
     * @Note Device in the data source needs to be updated to take effect.
     */
    public void release();

    /**
     * Checks whether the device is allocated.
     * 
     * @return <code>true</code> if the device is allocated, and <code>false</code> otherwise
     */
    public boolean isAllocated();

    /**
     * Gets the information of the device from the data source.
     * 
     * @return {@link DeviceInformation information} about the device
     */
    public DeviceInformation getInformation();
}
