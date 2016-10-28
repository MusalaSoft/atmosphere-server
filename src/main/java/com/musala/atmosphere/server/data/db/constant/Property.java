package com.musala.atmosphere.server.data.db.constant;

public class Property {
    public static final String DATABASE_URL_FORMAT = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=%d";

    public static final String DATABASE_NAME = "device_pool";

    public static final int DB_CLOSE_DELAY = 10;

    public static final String DATABASE_URL = String.format(Property.DATABASE_URL_FORMAT,
                                                            Property.DATABASE_NAME,
                                                            DB_CLOSE_DELAY);
}
