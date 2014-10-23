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
import com.musala.atmosphere.server.dao.IDeviceDao;
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
public class DeviceDao implements IDeviceDao {
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

    @Override
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

    @Override
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

    @Override
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
     * Selects device by RMI id.
     * 
     * @param rmiId
     *        - RMI id to be used as a match criterion
     * @return {@link IDevice device} matching the requested RMI id.
     * @throws DeviceDaoException
     *         - thrown when retrieving from the data source for the given RMI id fails
     */
    public IDevice selectByRmiId(String rmiId) throws DeviceDaoException {
        try {
            return getDeviceByFieldValue(DeviceColumnName.RMI_REGISTRY_ID, rmiId);
        } catch (SQLException e) {
            String message = String.format("Getting ID for device with RMI id %s failed, because data source failed.",
                                           rmiId);
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

    // Will be used by Device Pool Dao
    // TODO: Decide what kind of tests should be added - unit or integration. Fake database can be created for executing
    // the queries.
    /* package */List<IDevice> filterDevicesByParameters(DeviceParameters parameters) throws DeviceDaoException {
        Map<String, Object> queryMap = buildQueryMap(parameters);

        DeviceType deviceType = parameters.getDeviceType();
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
            String message = String.format("No device, matching the requested parameters %s, is found.", parameters);
            throw new DeviceDaoException(message, e);
        }
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

        return queryMap;
    }

    private List<Device> queryDevicesByParameters(Map<String, Object> query,
                                                  boolean isEmulator,
                                                  boolean queryAllDeviceTypes) throws SQLException {
        QueryBuilder<Device, String> deviceQueryBuilder = deviceDao.queryBuilder();

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

        // Devices first (is_emulator = 0) -> ascending order, emulators first (is_emulator = 1) -> descending order
        boolean isAscendingOrder = !isEmulator;
        deviceQueryBuilder.orderBy(DeviceColumnName.IS_EMULATOR, isAscendingOrder);

        deviceQueryBuilder.prepare();
        return deviceQueryBuilder.query();
    }
}
