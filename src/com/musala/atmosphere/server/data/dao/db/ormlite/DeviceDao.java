package com.musala.atmosphere.server.data.dao.db.ormlite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoRuntimeException;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
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

    /**
     * Gets all {@link IDevice devices} that match the given parameters.
     * 
     * @param parameters
     *        - the parameters to select devices by
     * @return a {@link List list} of devices matching the given parameters
     * @throws DeviceDaoException
     *         thrown when retrieving devices from the data source fails
     */
    public List<IDevice> filterDevicesByParameters(DeviceParameters parameters) throws DeviceDaoException {
        Map<String, Object> queryMap = buildQueryMap(parameters);
        DeviceType deviceType = parameters.getDeviceType();

        return queryDevicesByType(queryMap, deviceType);
    }

    /**
     * Gets all {@link IDevice devices} that match the given parameters and allocation criterion.
     * 
     * @param parameters
     *        - the parameters to select devices by
     * @param isAllocated
     *        - if <code>true</code> only allocated devices are filtered, otherwise devices are selected from the free
     *        ones
     * @return a {@link List list} of devices matching the given parameters
     * @throws DeviceDaoException
     *         thrown when retrieving devices from the data source fails
     */
    public List<IDevice> filterDevicesByParameters(DeviceParameters parameters, boolean isAllocated)
        throws DeviceDaoException {
        Map<String, Object> queryMap = buildQueryMap(parameters);
        queryMap.put(DeviceColumnName.IS_ALLOCATED, isAllocated);
        DeviceType deviceType = parameters.getDeviceType();

        return queryDevicesByType(queryMap, deviceType);
    }

    private List<IDevice> queryDevicesByType(Map<String, Object> queryMap, DeviceType deviceType)
        throws DeviceDaoException {
        boolean isEmulator = false;
        boolean queryAllDeviceTypes = true;

        try {
            switch (deviceType) {
                case DEVICE_ONLY:
                    isEmulator = false;
                    queryAllDeviceTypes = false;
                    break;
                case EMULATOR_ONLY:
                    isEmulator = true;
                    queryAllDeviceTypes = false;
                    break;
                case DEVICE_PREFERRED:
                    isEmulator = false;
                    queryAllDeviceTypes = true;
                    break;
                case EMULATOR_PREFERRED:
                    isEmulator = true;
                    queryAllDeviceTypes = true;
                    break;
                default:
                    break;
            }

            List<Device> deviceResultList = queryDevicesByParameters(queryMap, isEmulator, queryAllDeviceTypes);
            return new ArrayList<IDevice>(deviceResultList);
        } catch (SQLException e) {
            String message = String.format("No device, matching the given query %s and type %s.", queryMap, deviceType);
            throw new DeviceDaoException(message, e);
        }
    }

    private Device getDeviceByFieldValue(String fieldName, Object fieldValue) throws SQLException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(fieldName, fieldValue);

        List<Device> resultList = deviceDao.queryForFieldValuesArgs(query);

        if (!resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }

    private Map<String, Object> buildQueryMap(DeviceParameters parameters) {
        int apiLevel = parameters.getApiLevel();
        int dpi = parameters.getDpi();
        String model = parameters.getModel();
        DeviceOs os = parameters.getOs();
        int ram = parameters.getRam();
        int resolutionHeight = parameters.getResolutionHeight();
        int resolutionWidth = parameters.getResolutionWidth();
        String serialNumber = parameters.getSerialNumber();
        Boolean hasCamera = parameters.hasCameraPresent();

        Map<String, Object> queryMap = new HashMap<String, Object>();

        if (apiLevel != DeviceParameters.API_LEVEL_NO_PREFERENCE) {
            queryMap.put(DeviceColumnName.API_LEVEL, apiLevel);
        }
        if (dpi != DeviceParameters.DPI_NO_PREFERENCE) {
            queryMap.put(DeviceColumnName.DPI, dpi);
        }
        if (!model.equals(DeviceParameters.MODEL_NO_PREFERENCE)) {
            queryMap.put(DeviceColumnName.MODEL, model);
        }
        if (!os.equals(DeviceParameters.DEVICE_OS_NO_PREFERENCE)) {
            queryMap.put(DeviceColumnName.OS, os.toString());
        }
        if (ram != DeviceParameters.RAM_NO_PREFERENCE) {
            queryMap.put(DeviceColumnName.RAM, ram);
        }
        if (resolutionHeight != DeviceParameters.RESOLUTION_HEIGHT_NO_PREFERENCE) {
            queryMap.put(DeviceColumnName.RESOLUTION_HEIGHT, resolutionHeight);
        }
        if (resolutionWidth != DeviceParameters.RESOLUTION_WIDTH_NO_PREFERENCE) {
            queryMap.put(DeviceColumnName.RESOLUTION_WIDTH, resolutionWidth);
        }
        if (!serialNumber.equals(DeviceParameters.SERIALNUMBER_NO_PREFERENCE)) {
            queryMap.put(DeviceColumnName.SERIAL_NUMBER, serialNumber);
        }

        if (hasCamera != null && !hasCamera.equals(DeviceParameters.HAS_CAMERA_NO_PREFERENCE)) {
            queryMap.put(DeviceColumnName.HAS_CAMERA, hasCamera);
        }

        return queryMap;
    }

    private List<Device> queryDevicesByParameters(Map<String, Object> query,
                                                  boolean isEmulator,
                                                  boolean queryAllDeviceTypes) throws SQLException {

        // Results are ordered by is_emulator column. If devices are preferred, results are ordered in ascending order.
        // When emulators are preferred descending ordering is used.
        boolean isAscendingOrder = !isEmulator;
        QueryBuilder<Device, String> deviceQueryBuilder = deviceDao.queryBuilder();

        if (query.isEmpty() && queryAllDeviceTypes) {
            deviceQueryBuilder.orderBy(DeviceColumnName.IS_EMULATOR, isAscendingOrder);
            deviceQueryBuilder.prepare();
            return deviceQueryBuilder.query();
        }

        Set<Entry<String, Object>> querySet = query.entrySet();
        Iterator<Entry<String, Object>> iterator = querySet.iterator();
        Where<Device, String> queryWhereClause = deviceQueryBuilder.where();

        while (iterator.hasNext()) {
            Entry<String, Object> criterion = iterator.next();

            if (!iterator.hasNext() && queryAllDeviceTypes) {
                queryWhereClause = queryWhereClause.eq(criterion.getKey(), criterion.getValue());
            } else {
                queryWhereClause = queryWhereClause.eq(criterion.getKey(), criterion.getValue()).and();
            }
        }

        if (!queryAllDeviceTypes) {
            queryWhereClause = queryWhereClause.eq(DeviceColumnName.IS_EMULATOR, isEmulator);
        }

        deviceQueryBuilder.orderBy(DeviceColumnName.IS_EMULATOR, isAscendingOrder);

        deviceQueryBuilder.prepare();
        return deviceQueryBuilder.query();
    }
}
