package com.musala.atmosphere.server.util;

/**
 * Enumeration class containing all possible server properties.
 * 
 * @author valyo.yolovski
 * 
 */
public enum ServerProperties {
    DEVICE_REQUEST_TIMEOUT("device.request.timeout"),
    DEVICE_UPDATE_TIME("device.update.timeout"),
    EMULATOR_CREATION_TIMEOUT("emulator.creation.timeout"),
    WEBSOCKET_PORT("websocket.port"),
    SERVER_IP("server.ip");

    private String value;

    private ServerProperties(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
