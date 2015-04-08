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
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceOs;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.commons.util.Pair;
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
        Map<String, Pair<Object, WhereClauseOperator>> queryMap = buildQueryMap(parameters);

        return queryDevicesByType(queryMap, parameters);
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
        Map<String, Pair<Object, WhereClauseOperator>> queryMap = buildQueryMap(parameters);
        Pair<Object, WhereClauseOperator> isAlocatedPair = new Pair<Object, WhereClauseOperator>(isAllocated,
                                                                                                 WhereClauseOperator.EQUAL);
        queryMap.put(DeviceColumnName.IS_ALLOCATED, isAlocatedPair);

        return queryDevicesByType(queryMap, parameters);
    }

    private List<IDevice> queryDevicesByType(Map<String, Pair<Object, WhereClauseOperator>> queryMap,
                                             DeviceParameters parameters) throws DeviceDaoException {
        boolean isEmulator = false;
        boolean queryAllDeviceTypes = true;
        DeviceType deviceType = parameters.getDeviceType();

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

            List<Device> deviceResultList = queryDevicesByParameters(queryMap,
                                                                     isEmulator,
                                                                     queryAllDeviceTypes,
                                                                     parameters);
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

    private Map<String, Pair<Object, WhereClauseOperator>> buildQueryMap(DeviceParameters parameters) {
        int apiLevel = parameters.getTargetApiLevel();
        int dpi = parameters.getDpi();
        String model = parameters.getModel();
        DeviceOs os = parameters.getOs();
        int ram = parameters.getRam();
        int resolutionHeight = parameters.getResolutionHeight();
        int resolutionWidth = parameters.getResolutionWidth();
        String serialNumber = parameters.getSerialNumber();
        Boolean hasCamera = parameters.hasCameraPresent();
        int minApiLevel = parameters.getMinApiLevel();
        int maxApiLevel = parameters.getMaxApiLevel();

        Map<String, Pair<Object, WhereClauseOperator>> queryMap = new HashMap<String, Pair<Object, WhereClauseOperator>>();

        if (minApiLevel != DeviceParameters.MIN_API_LEVEL_NO_PREFERENCE
                && maxApiLevel != DeviceParameters.MAX_API_LEVEL_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> rangeApiLevelPair = new Pair<Object, WhereClauseOperator>(apiLevel,
                                                                                                        WhereClauseOperator.BETWEEN);
            queryMap.put(DeviceColumnName.API_LEVEL, rangeApiLevelPair);
        } else if (maxApiLevel != DeviceParameters.MAX_API_LEVEL_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> maxApiLevelPair = new Pair<Object, WhereClauseOperator>(maxApiLevel,
                                                                                                      WhereClauseOperator.LESS_OR_EQUAL);
            queryMap.put(DeviceColumnName.API_LEVEL, maxApiLevelPair);
        } else if (minApiLevel != DeviceParameters.MIN_API_LEVEL_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> minApiLevelPair = new Pair<Object, WhereClauseOperator>(minApiLevel,
                                                                                                      WhereClauseOperator.GREATER_OR_EQUAL);
            queryMap.put(DeviceColumnName.API_LEVEL, minApiLevelPair);
        } else if (apiLevel != DeviceParameters.TARGET_API_LEVEL_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> apiLevelPair = new Pair<Object, WhereClauseOperator>(apiLevel,
                                                                                                   WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.API_LEVEL, apiLevelPair);
        }

        if (dpi != DeviceParameters.DPI_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> dpiPair = new Pair<Object, WhereClauseOperator>(dpi,
                                                                                              WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.DPI, dpiPair);
        }

        if (!model.equals(DeviceParameters.MODEL_NO_PREFERENCE)) {
            Pair<Object, WhereClauseOperator> modelPair = new Pair<Object, WhereClauseOperator>(model,
                                                                                                WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.MODEL, modelPair);
        }

        if (!os.equals(DeviceParameters.DEVICE_OS_NO_PREFERENCE)) {
            Pair<Object, WhereClauseOperator> osPair = new Pair<Object, WhereClauseOperator>(os.toString(),
                                                                                             WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.OS, osPair);
        }

        if (ram != DeviceParameters.RAM_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> ramPair = new Pair<Object, WhereClauseOperator>(ram,
                                                                                              WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.RAM, ramPair);
        }

        if (resolutionHeight != DeviceParameters.RESOLUTION_HEIGHT_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> resolutionHeightPair = new Pair<Object, WhereClauseOperator>(resolutionHeight,
                                                                                                           WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.RESOLUTION_HEIGHT, resolutionHeightPair);
        }

        if (resolutionWidth != DeviceParameters.RESOLUTION_WIDTH_NO_PREFERENCE) {
            Pair<Object, WhereClauseOperator> resolutionWidthPair = new Pair<Object, WhereClauseOperator>(resolutionWidth,
                                                                                                          WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.RESOLUTION_WIDTH, resolutionWidthPair);
        }

        if (!serialNumber.equals(DeviceParameters.SERIALNUMBER_NO_PREFERENCE)) {
            Pair<Object, WhereClauseOperator> serialNumberPair = new Pair<Object, WhereClauseOperator>(serialNumber,
                                                                                                       WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.SERIAL_NUMBER, serialNumberPair);
        }

        if (hasCamera != null && !hasCamera.equals(DeviceParameters.HAS_CAMERA_NO_PREFERENCE)) {
            Pair<Object, WhereClauseOperator> hasCameraPair = new Pair<Object, WhereClauseOperator>(hasCamera,
                                                                                                    WhereClauseOperator.EQUAL);
            queryMap.put(DeviceColumnName.HAS_CAMERA, hasCameraPair);
        }

        return queryMap;
    }

    private List<Device> queryDevicesByParameters(Map<String, Pair<Object, WhereClauseOperator>> whereClausesQuery,
                                                  boolean isEmulator,
                                                  boolean queryAllDeviceTypes,
                                                  DeviceParameters parameters) throws SQLException {

        // Results are ordered by is_emulator column. If devices are preferred, results are ordered in ascending order.
        // When emulators are preferred descending ordering is used.
        boolean isAscendingOrder = !isEmulator;

        QueryBuilder<Device, String> deviceQueryBuilder = deviceDao.queryBuilder();

        // If DeviceParameters has set DEVICE_ONLY or EMULATOR_ONLY. Add them to the WhereClausesQuery
        if (!queryAllDeviceTypes) {
            Pair<Object, WhereClauseOperator> isEmulatorPair = new Pair<Object, WhereClauseOperator>(isEmulator,
                                                                                                     WhereClauseOperator.EQUAL);
            whereClausesQuery.put(DeviceColumnName.IS_EMULATOR, isEmulatorPair);
        }

        if (whereClausesQuery.isEmpty() && queryAllDeviceTypes) {
            deviceQueryBuilder.orderBy(DeviceColumnName.IS_EMULATOR, isAscendingOrder);
            deviceQueryBuilder.prepare();
            return deviceQueryBuilder.query();
        }

        int minApiLevel = parameters.getMinApiLevel();
        int maxApiLevel = parameters.getMaxApiLevel();
        int targetApiLevel = parameters.getTargetApiLevel();

        // Creates Query for selecting Device with the given TargetApiLevel if there is such.
        // It is used in order to be determined that there are No devices with the TargetApi and we can use the given
        // Range (Min, Max).
        if (targetApiLevel != DeviceParameters.TARGET_API_LEVEL_NO_PREFERENCE) {
            QueryBuilder<Device, String> deviceQueryBuilderTargetApiLevel = deviceDao.queryBuilder();
            deviceQueryBuilderTargetApiLevel.where().eq(DeviceColumnName.API_LEVEL, targetApiLevel);

            deviceQueryBuilderTargetApiLevel.prepare();
            List<Device> avaibleDevices = deviceQueryBuilderTargetApiLevel.query();

            // If there are available Devices For the TargetApiVersion Then Add it to the query map.
            if (!avaibleDevices.isEmpty()) {
                Pair<Object, WhereClauseOperator> apiLevelPair = new Pair<Object, WhereClauseOperator>(targetApiLevel,
                                                                                                       WhereClauseOperator.EQUAL);
                whereClausesQuery.put(DeviceColumnName.API_LEVEL, apiLevelPair);
            }
        }

        Where<Device, String> queryWhereClause = deviceQueryBuilder.where();
        Set<Entry<String, Pair<Object, WhereClauseOperator>>> querySet = whereClausesQuery.entrySet();
        Iterator<Entry<String, Pair<Object, WhereClauseOperator>>> iterator = querySet.iterator();

        // Builds WHERE Clauses
        while (iterator.hasNext()) {
            Entry<String, Pair<Object, WhereClauseOperator>> criterion = iterator.next();
            Pair<Object, WhereClauseOperator> criterionWhereClause = criterion.getValue();
            WhereClauseOperator criterionWhereClauseOperator = criterionWhereClause.getValue();

            switch (criterionWhereClauseOperator) {
                case EQUAL:
                    queryWhereClause = queryWhereClause.eq(criterion.getKey(), criterionWhereClause.getKey());
                    break;
                case GREATER_OR_EQUAL:
                    queryWhereClause = queryWhereClause.ge(criterion.getKey(), criterionWhereClause.getKey());
                    break;
                case LESS_OR_EQUAL:
                    queryWhereClause = queryWhereClause.le(criterion.getKey(), criterionWhereClause.getKey());
                    break;
                case BETWEEN:
                    queryWhereClause = queryWhereClause.between(criterion.getKey(), minApiLevel, maxApiLevel);
                    break;
                default:
                    break;
            }
        }

        int whereClausesCount = whereClausesQuery.size();
        // Add AND between the WhereClauses added in the QueryMap.
        queryWhereClause = queryWhereClause.and(whereClausesCount);

        deviceQueryBuilder.orderBy(DeviceColumnName.IS_EMULATOR, isAscendingOrder);

        deviceQueryBuilder.prepare();
        return deviceQueryBuilder.query();
    }
}
