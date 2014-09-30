package com.musala.atmosphere.server.dao.nativeobject;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.server.dao.IDeviceDAO;

/**
 * A data access object which handles allocating, releasing and getting a device's information.
 * 
 * @author yavor.stankov
 * 
 */
public class DeviceDao implements IDeviceDAO {
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
    public DeviceDao(DeviceInformation deviceInformation, String deviceId, String agentId) {
        deviceId = this.deviceId;
        deviceInformation = this.deviceInformation;
        agentId = this.agentId;
    }

    @Override
    public boolean allocate() {
        isAllocated = true;
        return true;
    }

    @Override
    public boolean release() {
        isAllocated = false;
        return true;
    }

    @Override
    public DeviceInformation getInformation() {
        return deviceInformation;
    }

    @Override
    public String getId() {
        return deviceId;
    }

    @Override
    public boolean isAllocated() {
        return isAllocated;
    }

    public String getAgentId() {
        return agentId;
    }
}
