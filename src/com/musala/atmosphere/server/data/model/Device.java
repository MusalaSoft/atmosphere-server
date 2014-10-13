package com.musala.atmosphere.server.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
import com.musala.atmosphere.server.data.db.constant.TableName;

/**
 * Entity representing a device, storing all the useful information about it.
 * 
 * @author filareta.yordanova
 * 
 */
@DatabaseTable(tableName = TableName.DEVICE)
public class Device {
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

    @DatabaseField(columnName = DeviceColumnName.AGENT, canBeNull = false, foreign = true)
    private Agent agent;

    @DatabaseField(columnName = DeviceColumnName.RMI_REGISTRY_ID, unique = true, canBeNull = false)
    private String rmiRegistryId;

    public Device() {
        // all persisted classes must define a no-arg constructor, used when an object is returned from a query
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
     * Gets the RMI registry id of this device.
     * 
     * @return the RMI registry of this device
     */
    public String getRmiRegistryId() {
        return rmiRegistryId;
    }

    /**
     * Sets the RMI registry id of this device.
     * 
     * @param rmiRegistryId
     *        - the RMI registry of this device
     */
    public void setRmiRegistryId(String rmiRegistryId) {
        this.rmiRegistryId = rmiRegistryId;
    }

    /**
     * Gets information whether this device is allocated.
     * 
     * @return <code>true</code> if this device is allocated, <code>false</code> otherwise
     */
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
    public Boolean getHasCamera() {
        return hasCamera;
    }

    /**
     * Sets whether this device has camera.
     * 
     * @param hasCamera
     *        - indicates whether this device has camera
     */
    public void setHasCamera(Boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

}