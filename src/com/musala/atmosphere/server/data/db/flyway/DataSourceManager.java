package com.musala.atmosphere.server.data.db.flyway;

import java.io.File;

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
    private static final String MIGRATIONS_LOCATION_PATTERN = "filesystem:resources" + File.separator + "db"
            + File.separator + "migration";

    private DataSourceCallback dataSourceCallback;

    private Flyway flywayDataHandler;

    public DataSourceManager(DataSourceCallback dataSourceCallback) {
        this.dataSourceCallback = dataSourceCallback;
        flywayDataHandler = new Flyway();
    }

    @Override
    public void initialize() {
        flywayDataHandler.setDataSource(Property.DATABASE_URL, null, null);

        // Drops the database if it already exists.
        flywayDataHandler.clean();
        flywayDataHandler.setCallbacks(dataSourceCallback);

        String migrationsLocation = String.format(MIGRATIONS_LOCATION_PATTERN, File.separator, File.separator);
        flywayDataHandler.setLocations(migrationsLocation);

        flywayDataHandler.migrate();
    }
}
