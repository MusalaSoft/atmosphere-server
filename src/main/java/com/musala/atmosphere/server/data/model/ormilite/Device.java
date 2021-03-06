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

package com.musala.atmosphere.server.data.model.ormilite;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.data.db.constant.AgentColumnName;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
import com.musala.atmosphere.server.data.db.constant.TableName;
import com.musala.atmosphere.server.data.model.IDevice;

/**
 * Entity representing a device, storing all the useful information about it.
 *
 * @author filareta.yordanova
 *
 */
@DatabaseTable(tableName = TableName.DEVICE)
public class Device implements IDevice {
    @DatabaseField(columnName = DeviceColumnName.ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = DeviceColumnName.SERIAL_NUMBER, index = true, canBeNull = false)
    private String serialNumber;

    @DatabaseField(columnName = DeviceColumnName.IS_EMULATOR, canBeNull = false, defaultValue = "0")
    private boolean isEmulator;

    @DatabaseField(columnName = DeviceColumnName.IS_TABLET)
    private Boolean isTablet;

    @DatabaseField(columnName = DeviceColumnName.RESOLUTION_HEIGHT)
    private Integer resolutionHeight;

    @DatabaseField(columnName = DeviceColumnName.RESOLUTION_WIDTH)
    private Integer resolutionWidth;

    @DatabaseField(columnName = DeviceColumnName.OS)
    private String os;

    @DatabaseField(columnName = DeviceColumnName.MODEL)
    private String model;

    @DatabaseField(columnName = DeviceColumnName.DPI)
    private Integer dpi;

    @DatabaseField(columnName = DeviceColumnName.RAM)
    private Integer ram;

    @DatabaseField(columnName = DeviceColumnName.CPU)
    private String cpu;

    @DatabaseField(columnName = DeviceColumnName.API_LEVEL)
    private Integer apiLevel;

    @DatabaseField(columnName = DeviceColumnName.MANIFACTURER)
    private String manufacturer;

    @DatabaseField(columnName = DeviceColumnName.HAS_CAMERA)
    private Boolean hasCamera;

    @DatabaseField(columnName = DeviceColumnName.IS_ALLOCATED, defaultValue = "0", canBeNull = false)
    private boolean isAllocated;

    @DatabaseField(columnName = DeviceColumnName.AGENT, canBeNull = false, foreign = true, foreignAutoRefresh = true, foreignColumnName = AgentColumnName.ID)
    private Agent agent;

    @DatabaseField(columnName = DeviceColumnName.DEVICE_ID, unique = true, canBeNull = false)
    private String deviceId;

    @DatabaseField(columnName = DeviceColumnName.PASSKEY, canBeNull = false)
    private long passkey;

    public Device() {
        // all persisted classes must define a no-arg constructor, used when an object is returned from a query
    }

    /**
     * Creates new device with the given serial number and an identifier.
     *
     * @param serialNumber
     *        - the serial number of this device
     * @param deviceId
     *        - the identifier of this device
     */
    public Device(String serialNumber, String deviceId) {
        this.serialNumber = serialNumber;
        this.deviceId = deviceId;
    }

    /**
     * Creates new device with the given serial number, device identifier and passkey.
     *
     * @param deviceInformation
     *        - the {@link DeviceInformation information} of this device
     * @param deviceId
     *        - the identifier of this device
     * @param passkeyAuthority
     *        - a passkey for validating authority
     */
    public Device(DeviceInformation deviceInformation, String deviceId, long passkeyAuthority) {
        apiLevel = deviceInformation.getApiLevel();
        cpu = deviceInformation.getCpu();
        dpi = deviceInformation.getDpi();
        manufacturer = deviceInformation.getManufacturer();
        model = deviceInformation.getModel();
        os = deviceInformation.getOS();
        ram = deviceInformation.getRam();
        Pair<Integer, Integer> resolution = deviceInformation.getResolution();
        resolutionWidth = resolution.getValue();
        resolutionHeight = resolution.getKey();
        serialNumber = deviceInformation.getSerialNumber();
        isEmulator = deviceInformation.isEmulator();
        isTablet = deviceInformation.isTablet();
        hasCamera = deviceInformation.hasCamera();
        this.deviceId = deviceId;
        passkey = passkeyAuthority;
    }

    /**
     * Gets the agent responsible for this device.
     *
     * @return the agent for this device
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Sets the agent responsible for this device.
     *
     * @param agent
     *        - the agent for this device
     */
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * Gets the ID of the device entry.
     *
     * @return the ID of the device entry
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the device entry.
     *
     * @param id
     *        - the ID of the device entry
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the identifier of this device.
     *
     * @param deviceId
     *        - the identifier of this device
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets information whether this device is allocated.
     *
     * @return <code>true</code> if this device is allocated, <code>false</code> otherwise
     */
    @Override
    public boolean isAllocated() {
        return isAllocated;
    }

    /**
     * Sets whether this device is allocated.
     *
     * @param isAllocated
     *        - indicates whether this device is allocated
     */
    public void setAllocated(boolean isAllocated) {
        this.isAllocated = isAllocated;
    }

    /**
     * Gets the serial number of this device.
     *
     * @return the serial number of this device
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number for this device.
     *
     * @param serialNumber
     *        - the serial number of this device
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets information whether this device is an emulator.
     *
     * @return <code>true</code> if this device is an emulator, <code>false</code> otherwise
     */
    public boolean isEmulator() {
        return isEmulator;
    }

    /**
     * Sets whether this device is an emulator.
     *
     * @param isEmulator
     *        - indicates whether this device is an emulator
     */
    public void setEmulator(boolean isEmulator) {
        this.isEmulator = isEmulator;
    }

    /**
     * Gets information whether this device is a tablet.
     *
     * @return <code>true</code> if this device is a tablet, <code>false</code> otherwise
     */
    public Boolean isTablet() {
        return isTablet;
    }

    /**
     * Sets whether this device is a tablet.
     *
     * @param isTablet
     *        - indicates whether this device is a tablet
     */
    public void setTablet(Boolean isTablet) {
        this.isTablet = isTablet;
    }

    /**
     * Gets the resolution height of this device.
     *
     * @return the resolution height of this device
     */
    public Integer getResolutionHeight() {
        return resolutionHeight;
    }

    /**
     * Sets the resolution height for this device.
     *
     * @param resolutionHeight
     *        - the resolution height of this device
     */
    public void setResolutionHeight(Integer resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

    /**
     * Gets the resolution width of this device.
     *
     * @return the resolution width of this device
     */
    public Integer getResolutionWidth() {
        return resolutionWidth;
    }

    /**
     * Sets the resolution width for this device.
     *
     * @param resolutionWidth
     *        - the resolution width of this device
     */
    public void setResolutionWidth(Integer resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }

    /**
     * Gets the operating system version of this device.
     *
     * @return the operating system of this device
     */
    public String getOs() {
        return os;
    }

    /**
     * Sets the operating system for this device.
     *
     * @param os
     *        - the operating system of this device
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * Gets the model of this device.
     *
     * @return the model of this device
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model for this device.
     *
     * @param model
     *        - the model of this device
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Gets the DPI of this device.
     *
     * @return the DPI of this device
     */
    public Integer getDpi() {
        return dpi;
    }

    /**
     * Sets the DPI for this device.
     *
     * @param dpi
     *        - the DPI of this device
     */
    public void setDpi(Integer dpi) {
        this.dpi = dpi;
    }

    /**
     * Gets the RAM of this device.
     *
     * @return the RAM of this device
     */
    public Integer getRam() {
        return ram;
    }

    /**
     * Sets the RAM for this device.
     *
     * @param ram
     *        - the RAM of this device
     */
    public void setRam(Integer ram) {
        this.ram = ram;
    }

    /**
     * Gets the CPU of this device.
     *
     * @return the CPU of this device
     */
    public String getCpu() {
        return cpu;
    }

    /**
     * Sets the CPU for this device.
     *
     * @param cpu
     *        - the CPU of this device
     */
    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    /**
     * Gets the API level of this device.
     *
     * @return the API level of this device
     */
    public Integer getApiLevel() {
        return apiLevel;
    }

    /**
     * Sets the API level for this device.
     *
     * @param apiLevel
     *        - the API level of this device
     */
    public void setApiLevel(Integer apiLevel) {
        this.apiLevel = apiLevel;
    }

    /**
     * Gets the manufacturer of this device.
     *
     * @return the manufacturer of this device
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the manufacturer for this device.
     *
     * @param manufacturer
     *        - the manufacturer of this device
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Gets information whether this device has camera.
     *
     * @return <code>true</code> if this device has camera, <code>false</code> otherwise
     */
    public Boolean hasCamera() {
        return hasCamera;
    }

    /**
     * Sets whether this device has camera.
     *
     * @param hasCamera
     *        - indicates whether this device has camera
     */
    public void setCamera(Boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

    /**
     * Gets the passkey for this device.
     *
     * @return the passkey of this device
     */
    @Override
    public long getPasskey() {
        return passkey;
    }

    /**
     * Sets the passkey for this device.
     *
     * @param passkey
     *        - the passkey for this device
     */
    @Override
    public void setPasskey(long passkey) {
        this.passkey = passkey;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(agent).append(deviceId).append(serialNumber).toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null || !getClass().equals(object.getClass())) {
            return false;
        }

        Device device = (Device) object;

        return new EqualsBuilder().append(agent, device.agent)
                                  .append(deviceId, device.deviceId)
                                  .append(serialNumber, device.serialNumber)
                                  .isEquals();
    }

    @Override
    public void allocate() {
        setAllocated(true);
    }

    @Override
    public void release() {
        setAllocated(false);
    }

    @Override
    public DeviceInformation getInformation() {
        DeviceInformation deviceInformation = new DeviceInformation();

        deviceInformation.setEmulator(isEmulator);
        deviceInformation.setSerialNumber(serialNumber);

        if (apiLevel != null) {
            deviceInformation.setApiLevel(apiLevel);
        }
        if (hasCamera != null) {
            deviceInformation.setCamera(hasCamera);
        }
        if (cpu != null) {
            deviceInformation.setCpu(cpu);
        }
        if (dpi != null) {
            deviceInformation.setDpi(dpi);
        }
        if (manufacturer != null) {
            deviceInformation.setManufacturer(manufacturer);
        }
        if (model != null) {
            deviceInformation.setModel(model);
        }
        if (os != null) {
            deviceInformation.setOs(os);
        }
        if (ram != null) {
            deviceInformation.setRam(ram);
        }
        if (resolutionWidth != null && resolutionHeight != null) {
            Pair<Integer, Integer> resolution = new Pair<>(resolutionWidth, resolutionWidth);
            deviceInformation.setResolution(resolution);
        }
        if (isTablet != null) {
            deviceInformation.setTablet(isTablet);
        }

        return deviceInformation;
    }

    @Override
    public String getAgentId() {
        if (agent != null) {
            return agent.getAgentId();
        }

        return null;
    }

    @Override
    public String getDeviceId() {
        // For now the unique identifier for the device is its device id (agentId + "_" + deviceSerial).
        return deviceId;
    }

    @Override
    public void setDeviceInformation(DeviceInformation information) {
        apiLevel = information.getApiLevel();
        cpu = information.getCpu();
        dpi = information.getDpi();
        manufacturer = information.getManufacturer();
        model = information.getModel();
        os = information.getOS();
        ram = information.getRam();
        Pair<Integer, Integer> resolution = information.getResolution();
        resolutionWidth = resolution.getValue();
        resolutionHeight = resolution.getKey();
        serialNumber = information.getSerialNumber();
        isEmulator = information.isEmulator();
        isTablet = information.isTablet();
        hasCamera = information.hasCamera();
    }
}
