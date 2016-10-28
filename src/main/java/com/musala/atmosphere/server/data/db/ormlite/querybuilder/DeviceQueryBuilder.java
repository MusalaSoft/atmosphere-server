package com.musala.atmosphere.server.data.db.ormlite.querybuilder;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel.Maximum;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel.Minimum;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel.Target;
import com.musala.atmosphere.commons.cs.deviceselection.CameraAvailable;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceModel;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceOs;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceParameter.Visitor;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceType;
import com.musala.atmosphere.commons.cs.deviceselection.RamCapacity;
import com.musala.atmosphere.commons.cs.deviceselection.ScreenParameter.DPI;
import com.musala.atmosphere.commons.cs.deviceselection.ScreenParameter.Height;
import com.musala.atmosphere.commons.cs.deviceselection.ScreenParameter.Width;
import com.musala.atmosphere.commons.cs.deviceselection.SerialNumber;
import com.musala.atmosphere.commons.cs.exception.DeviceSelectionFailedException;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
import com.musala.atmosphere.server.data.model.ormilite.Device;

/**
 * Class responsible for building query for filtering devices by a given selector. This query builder gives priority to
 * the {@link ApiLevel.Target target API level} if such criterion for selection is set, without looking the
 * {@link ApiLevel.Minimum minimum} and {@link ApiLevel.Maximum maximum} API levels.
 * 
 * @author filareta.yordanova
 * 
 */
public class DeviceQueryBuilder implements Visitor {
    protected final static String ERROR_MESSAGE_PATTERN = "Failed to append criterion for the property %s with value %s to the device selection query.";

    protected QueryBuilder<Device, String> deviceQueryBuilder;

    protected Where<Device, String> deviceWhereClause;

    protected int criteriaCount;

    private boolean isEmulator;

    private boolean withPreference;

    /**
     * Creates builder for device selection query by a given device data access object and {@link DeviceSelector
     * selector}.
     * 
     * @param deviceDao
     *        - object for accessing data about stored devices in the database
     * @param selector
     *        - contains all device parameters used for device matching
     */
    public DeviceQueryBuilder(Dao<Device, String> deviceDao, DeviceSelector selector) {
        deviceQueryBuilder = deviceDao.queryBuilder();
        deviceWhereClause = deviceQueryBuilder.where();
        criteriaCount = 0;

        selector.visitAll(this);
    }

    @Override
    public void visit(Minimum minApiLevel) {
    }

    @Override
    public void visit(Maximum maxApiLevel) {
    }

    @Override
    public void visit(Target targetApiLevel) {
        appendWhereClause(DeviceColumnName.API_LEVEL, targetApiLevel.getValue());
    }

    @Override
    public void visit(DeviceOs deviceOs) {
        appendWhereClause(DeviceColumnName.OS, deviceOs);
    }

    @Override
    public void visit(DeviceType deviceType) {
        switch (deviceType) {
            case DEVICE_ONLY:
                isEmulator = false;
                withPreference = false;
                break;
            case EMULATOR_ONLY:
                isEmulator = true;
                withPreference = false;
                break;
            case DEVICE_PREFERRED:
                isEmulator = false;
                withPreference = true;
                break;
            case EMULATOR_PREFERRED:
                isEmulator = true;
                withPreference = true;
                break;
            default:
                break;
        }

        if (!withPreference) {
            appendWhereClause(DeviceColumnName.IS_EMULATOR, isEmulator);
        }
    }

    @Override
    public void visit(SerialNumber serialNumber) {
        appendWhereClause(DeviceColumnName.SERIAL_NUMBER, serialNumber.getValue());
    }

    @Override
    public void visit(DeviceModel deviceModel) {
        appendWhereClause(DeviceColumnName.MODEL, deviceModel.getValue());
    }

    @Override
    public void visit(Width screenWidth) {
        appendWhereClause(DeviceColumnName.RESOLUTION_WIDTH, screenWidth.getValue());
    }

    @Override
    public void visit(Height screenHeight) {
        appendWhereClause(DeviceColumnName.RESOLUTION_HEIGHT, screenHeight.getValue());
    }

    @Override
    public void visit(DPI screenDpi) {
        appendWhereClause(DeviceColumnName.DPI, screenDpi.getValue());
    }

    @Override
    public void visit(CameraAvailable cameraAvailable) {
        appendWhereClause(DeviceColumnName.HAS_CAMERA, cameraAvailable.getValue());
    }

    @Override
    public void visit(RamCapacity ramCapacity) {
        appendWhereClause(DeviceColumnName.RAM, ramCapacity.getValue());
    }

    public void setAllocationCriterion(Boolean isAllocated) {
        appendWhereClause(DeviceColumnName.IS_ALLOCATED, isAllocated);
    }

    public void setDeviceTypeCriterion(Boolean isEmulator) {
        appendWhereClause(DeviceColumnName.IS_EMULATOR, isEmulator);
    }

    /**
     * Prepares the device selection query, appends all the where clauses and sets the ordering of the retrieved
     * results.
     * 
     * @return {@link QueryBuilder query builder} instance for the {@link Device device} object
     * @throws SQLException
     *         if one of the operations with the query fails
     */
    public QueryBuilder<Device, String> prepareQuery() throws SQLException {
        deviceWhereClause.and(criteriaCount);

        boolean isAscendingOrder = !isEmulator;
        deviceQueryBuilder.orderBy(DeviceColumnName.IS_EMULATOR, isAscendingOrder).distinct();

        return deviceQueryBuilder;
    }

    private void appendWhereClause(String columnName, Object criterion) {
        try {
            deviceWhereClause.eq(columnName, criterion);
            criteriaCount++;
        } catch (SQLException e) {
            String errorMessage = String.format(ERROR_MESSAGE_PATTERN, columnName, criterion);
            throw new DeviceSelectionFailedException(errorMessage, e);
        }
    }
}
