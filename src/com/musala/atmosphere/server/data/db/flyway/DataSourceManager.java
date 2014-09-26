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
    private static final String DATABASE_URL = String.format(Property.DATABASE_URL_FORMAT, Property.DATABASE_NAME);

    private static final String MIGRATIONS_LOCATION_PATTERN = "filesystem:%s/resources/db/migration";

    @Override
    public void initialize() {
        Flyway flywayDataHandler = new Flyway();
        flywayDataHandler.setDataSource(DATABASE_URL, null, null);
        flywayDataHandler.setCallbacks(new DataSourceCallback());

        String currentDir = System.getProperty("user.dir");
        String migrationsLocation = String.format(MIGRATIONS_LOCATION_PATTERN, currentDir);
        flywayDataHandler.setLocations(migrationsLocation);

        flywayDataHandler.migrate();
    }
}
