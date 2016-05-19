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
