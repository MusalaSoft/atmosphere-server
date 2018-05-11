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

package com.musala.atmosphere.server.data.db.flyway;

import java.sql.Connection;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;

import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;

/**
 * Class that receives callback functions after executing actions with the data source.
 *
 * @author filareta.yordanova
 *
 */
public class DataSourceCallback implements FlywayCallback {
    private ServerEventService eventService;

    public DataSourceCallback() {
        eventService = new ServerEventService();
    }

    @Override
    public void afterClean(Connection connection) {

    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo arg1) {

    }

    @Override
    public void afterInfo(Connection connection) {

    }

    @Override
    public void afterInit(Connection connection) {

    }

    @Override
    public void afterMigrate(Connection connection) {
        // Publish DataSourceInitializedEvent to notify that data sources must be created and ready to input data.
        DataSourceInitializedEvent dataSourceInitializedEvent = new DataSourceInitializedEvent();
        eventService.publish(dataSourceInitializedEvent);
    }

    @Override
    public void afterRepair(Connection connection) {

    }

    @Override
    public void afterValidate(Connection connection) {

    }

    @Override
    public void beforeClean(Connection connection) {

    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo migrationInfo) {

    }

    @Override
    public void beforeInfo(Connection connection) {

    }

    @Override
    public void beforeInit(Connection connection) {

    }

    @Override
    public void beforeMigrate(Connection connection) {

    }

    @Override
    public void beforeRepair(Connection connection) {

    }

    @Override
    public void beforeValidate(Connection connection) {

    }

}
