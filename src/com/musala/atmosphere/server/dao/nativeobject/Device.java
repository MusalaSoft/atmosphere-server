package com.musala.atmosphere.server.dao.nativeobject;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.server.data.model.IDevice;

/**
 * A data access object which handles allocating, releasing and getting a device's information.
 * 
 * @author yavor.stankov
 * 
 */
public class Device implements IDevice {
    private DeviceInformation deviceInformation;

    private String deviceId;

    private String agentId;

    private boolean isAllocated;

    /**
     * Creates new data access object which can be used to allocate, release or get a device's information using the
     * given parameters.
     * 
     * @param deviceInformation
     *        - {@link DeviceInformation} the information that should be stored for the device
     * @param deviceId
     *        - RMI {@link String} identifier for the device
     * @param agentId
     *        - the ID of the agent that the device is connected to
     */
    public Device(DeviceInformation deviceInformation, String deviceId, String agentId) {
        this.deviceId = deviceId;
        this.deviceInformation = deviceInformation;
        this.agentId = agentId;
    }

    @Override
    public void allocate() {
        isAllocated = true;
    }

    @Override
    public void release() {
        isAllocated = false;
    }

    @Override
    public DeviceInformation getInformation() {
        return deviceInformation;
    }

    @Override
    public boolean isAllocated() {
        return isAllocated;
    }

    /**
     * Gets the ID of this device.
     * 
     * @return ID for this device
     */
    public String getId() {
        return deviceId;
    }

    /**
     * Gets the ID of the agent on which this device is running.
     * 
     * @return ID of the agent responsible for this device
     */
    public String getAgentId() {
        return agentId;
    }
}
