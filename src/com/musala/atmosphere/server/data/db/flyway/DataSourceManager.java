package com.musala.atmosphere.server.data.db.flyway;

import org.flywaydb.core.Flyway;

import com.musala.atmosphere.server.data.IDataSourceManager;
import com.musala.atmosphere.server.data.db.constant.Property;

/**
 * Class that manages operation with the data source, such as initializing and migrating data, creating objects for data
 * access.
 * 
 * @author filareta.yordanova
 * 
 */
public class DataSourceManager implements IDataSourceManager {
    private static final String databaseUrl = String.format(Property.DATABASE_URL_FORMAT, Property.DATABASE_NAME);

    @Override
    public void initialize() {
        Flyway flywayDataHandler = new Flyway();
        flywayDataHandler.setDataSource(databaseUrl, null, null);
        flywayDataHandler.setCallbacks(new DataSourceCallback());
        flywayDataHandler.migrate();
    }
}
