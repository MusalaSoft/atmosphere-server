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

package com.musala.atmosphere.server.data.db.ormlite.querybuilder;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel.Maximum;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel.Minimum;
import com.musala.atmosphere.commons.cs.deviceselection.ApiLevel.Target;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.exception.DeviceSelectionFailedException;
import com.musala.atmosphere.server.data.db.constant.DeviceColumnName;
import com.musala.atmosphere.server.data.model.ormilite.Device;

/**
 * Class responsible for building query for filtering devices by a given selector. This query builder gives priority to
 * the {@link ApiLevel.Minimum minimum} and {@link ApiLevel.Maximum maximum} API levels if such criterion for selection
 * is set.
 * 
 * @author filareta.yordanova
 * 
 */
public class DeviceRangeQueryBuilder extends DeviceQueryBuilder {

    /**
     * Creates builder for device selection query by a given device data access object and {@link DeviceSelector
     * selector}.
     * 
     * @param deviceDao
     *        - object for accessing data about stored devices in the database
     * @param selector
     *        - contains all device parameters used for device matching
     */
    public DeviceRangeQueryBuilder(Dao<Device, String> deviceDao, DeviceSelector selector) {
        super(deviceDao, selector);
    }

    @Override
    public void visit(Minimum minApiLevel) {
        try {
            deviceWhereClause.ge(DeviceColumnName.API_LEVEL, minApiLevel.getValue());
            criteriaCount++;
        } catch (SQLException e) {
            String errorMessage = String.format(ERROR_MESSAGE_PATTERN,
                                                DeviceColumnName.API_LEVEL,
                                                minApiLevel.getValue());
            throw new DeviceSelectionFailedException(errorMessage, e);
        }
    }

    @Override
    public void visit(Maximum maxApiLevel) {
        try {
            deviceWhereClause.le(DeviceColumnName.API_LEVEL, maxApiLevel.getValue());
            criteriaCount++;
        } catch (SQLException e) {
            String errorMessage = String.format(ERROR_MESSAGE_PATTERN,
                                                DeviceColumnName.API_LEVEL,
                                                maxApiLevel.getValue());
            throw new DeviceSelectionFailedException(errorMessage, e);
        }
    }

    @Override
    public void visit(Target targetApiLevel) {
    }
}
