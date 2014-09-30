package com.musala.atmosphere.server.data.db.constant;

public class Property {
    public static final String DATABASE_URL_FORMAT = "jdbc:h2:mem:%s";

    public static final String DATABASE_NAME = "device_pool";

    public static final String DATABASE_URL = String.format(Property.DATABASE_URL_FORMAT, Property.DATABASE_NAME);
}
