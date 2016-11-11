package com.musala.atmosphere.server.data.db.ormlite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceParameter;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.DeviceSelectionFailedException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
import com.musala.atmosphere.server.data.db.ormlite.querybuilder.DeviceQueryBuilder;
import com.musala.atmosphere.server.data.db.ormlite.querybuilder.DeviceRangeQueryBuilder;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Device;

/**
 * Common class that provides data access object for executing operations with devices from the data source.
 *
 * @author filareta.yordanova
 *
 */
public class DeviceDao {
    private Dao<Device, String> deviceDao;

    /**
     * Creates new DeviceDao with the given data access object.
     *
     * @param deviceDao
     *        - data access object responsible for operations with devices from the data source
     * @throws SQLException
     *         - an exception that provides information on a database access error or other errors.
     */
    public DeviceDao(Dao<Device, String> deviceDao) throws SQLException {
        this.deviceDao = deviceDao;
    }

    /**
     * Updates device properties in the data source.
     *
     * @param device
     *        - device that will be updated in the data source
     * @throws DeviceDaoException
     *         thrown when updating device fails
     *
     */
    public void update(IDevice device) throws DeviceDaoException {
        if (device == null) {
            throw new DeviceDaoRuntimeException("You are trying to update device that is not present.");
        }

        try {
            deviceDao.update((Device) device);
        } catch (SQLException e) {
            DeviceInformation deviceInformation = device.getInformation();
            String message = String.format("Updating device with serial number %s failed, because data source failed.",
                                           deviceInformation.getSerialNumber());
            throw new DeviceDaoException(message, e);
        }
    }

    /**
     * Adds new device in the data source.
     *
     * @param device
     *        - device to be added in the data source
     * @throws DeviceDaoException
     *         thrown when adding new device fails
     */
    public void add(IDevice device) throws DeviceDaoException {
        if (device == null) {
            throw new DeviceDaoRuntimeException("The device you are trying to add is null.");
        }

        try {
            deviceDao.create((Device) device);
        } catch (SQLException e) {
            DeviceInformation deviceInformation = device.getInformation();
            String message = String.format("Adding device with serial number %s failed",
                                           deviceInformation.getSerialNumber());
            throw new DeviceDaoException(message, e);
        }
    }

    /**
     * Removes a device with the given ID from the data source.
     *
     * @param deviceId
     *        - the ID of the device to be removed
     * @throws DeviceDaoException
     *         thrown when removing device fails
     */
    public void remove(String deviceId) throws DeviceDaoException {
        try {
            Device deviceToRemove = getDeviceByFieldValue(DeviceColumnName.RMI_REGISTRY_ID, deviceId);

            if (deviceToRemove == null) {
                return;
            }

            deviceDao.delete(deviceToRemove);
        } catch (SQLException e) {
            String message = String.format("Removing device with ID %s failed, because data source failed.", deviceId);
            throw new DeviceDaoException(message, e);
        }
    }

    /**
     * Selects device by its unique ID.
     *
     * @param id
     *        - ID to be used as a match criterion
     * @return {@link IDevice device} matching the requested ID, or <code>null</code> if no device with such ID is found
     * @throws DeviceDaoException
     *         thrown when retrieving device from the data source fails
     */
    public IDevice selectById(String id) throws DeviceDaoException {
        try {
            return getDeviceByFieldValue(DeviceColumnName.RMI_REGISTRY_ID, id);
        } catch (SQLException e) {
            String message = String.format("Getting ID for device with RMI id %s failed, because data source failed.",
                                           id);
            throw new DeviceDaoException(message, e);
        }
    }

    private Device getDeviceByFieldValue(String fieldName, Object fieldValue) throws SQLException {
        Map<String, Object> query = new HashMap<>();
        query.put(fieldName, fieldValue);

        List<Device> resultList = deviceDao.queryForFieldValuesArgs(query);

        if (!resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }

    /**
     * Gets all {@link IDevice devices} that match the given {@link DeviceSelector selector} and allocation criterion.
     *
     * @param deviceSelector
     *        - contains all parameters for device filtering
     * @param isAllocated
     *        - if <code>true</code> only allocated devices are filtered, otherwise devices are selected from the free
     *        ones
     * @return a {@link List list} of devices matching the given parameters
     * @throws DeviceDaoException
     *         thrown when retrieving devices from the data source fails
     */
    public List<IDevice> filterDevices(DeviceSelector deviceSelector, boolean isAllocated) throws DeviceDaoException {
        DeviceQueryBuilder deviceQueryBuilder;
        List<IDevice> devices = new ArrayList<IDevice>();
        Map<Class<? extends DeviceParameter>, DeviceParameter> deviceParameters = deviceSelector.getParameters();

        try {
            // Target is with priority, if both target and range are set from the client.
            if (deviceParameters.containsKey(ApiLevel.Target.class)) {
                deviceQueryBuilder = new DeviceQueryBuilder(deviceDao, deviceSelector);
                devices = queryDevices(deviceQueryBuilder, isAllocated);
            }

            // If there are no results for the given target, try to find matching devices in the given API levels
            // range.
            if (devices.isEmpty()) {
                deviceQueryBuilder = new DeviceRangeQueryBuilder(deviceDao, deviceSelector);
                devices = queryDevices(deviceQueryBuilder, isAllocated);
            }

            return devices;
        } catch (SQLException | DeviceSelectionFailedException e) {
            String message = String.format("Retrieving devices for the given selector %s failed.", deviceSelector);
            throw new DeviceDaoException(message, e);
        }
    }

    private List<IDevice> queryDevices(DeviceQueryBuilder deviceQueryBuilder, boolean isAllocated) throws SQLException {
        deviceQueryBuilder.setAllocationCriterion(isAllocated);
        QueryBuilder<Device, String> queryBuilder = deviceQueryBuilder.prepareQuery();

        return new ArrayList<IDevice>(queryBuilder.query());
    }
}
