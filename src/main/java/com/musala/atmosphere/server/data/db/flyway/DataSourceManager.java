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
    private static final String MIGRATIONS_LOCATION_PATTERN = "classpath:db/migration";

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
