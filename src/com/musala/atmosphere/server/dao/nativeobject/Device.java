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

    private long passkey;

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
     * @param passkey
     *        - passkey for validating authority
     */
    public Device(DeviceInformation deviceInformation, String deviceId, String agentId, long passkey) {
        this.deviceId = deviceId;
        this.deviceInformation = deviceInformation;
        this.agentId = agentId;
        this.passkey = passkey;
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

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getPasskey() {
        return passkey;
    }
}
