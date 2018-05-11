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

package com.musala.atmosphere.server.data.db.constant;

public class Property {
    public static final String DATABASE_URL_FORMAT = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=%d";

    public static final String DATABASE_NAME = "device_pool";

    public static final int DB_CLOSE_DELAY = 10;

    public static final String DATABASE_URL = String.format(Property.DATABASE_URL_FORMAT,
                                                            Property.DATABASE_NAME,
                                                            DB_CLOSE_DELAY);
}
