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

package com.musala.atmosphere.server.util;

import com.musala.atmosphere.commons.util.PropertiesLoader;

/**
 * Reads server properties from server properties config file.
 *
 * @author valyo.yolovski
 *
 */
public class ServerPropertiesLoader {
    private static final String SERVER_PROPERTIES_FILE = "./server.properties";

    private synchronized static String getPropertyString(ServerProperties property) {
        PropertiesLoader propertiesLoader = PropertiesLoader.getInstance(SERVER_PROPERTIES_FILE);

        String propertyString = property.toString();

        String resultProperty = propertiesLoader.getPropertyString(propertyString);

        return resultProperty;
    }

    /**
     * Returns the time in milliseconds between two consecutive updates on the table with devices' timeout values.
     *
     * @return - a device update time
     */
    public static int getDeviceUpdateTime() {
        String valueAsString = getPropertyString(ServerProperties.DEVICE_UPDATE_TIME);
        int valueAsInt = Integer.parseInt(valueAsString);
        return valueAsInt;
    }

    /**
     * Returns the maximal number of updates on the table with timeout values of devices, before an unused device will
     * be freed.
     *
     * @return - a device request timeout
     */
    public static int getDeviceRequestTimeout() {
        String valueAsString = getPropertyString(ServerProperties.DEVICE_REQUEST_TIMEOUT);
        int valueAsInt = Integer.parseInt(valueAsString);
        return valueAsInt;
    }

    /**
     * Gets the maximum time in milliseconds that will pass before an emulator creation is declared a failure.
     *
     * @return the maximum time in milliseconds that will pass before an emulator creation is declared a failure.
     */
    public static long getEmulatorCreationTimeout() {
        String emulatorCreationTimeoutString = getPropertyString(ServerProperties.EMULATOR_CREATION_TIMEOUT);
        long emulatorCreationTimeout = Long.valueOf(emulatorCreationTimeoutString);
        return emulatorCreationTimeout;
    }

    /**
     * Gets the Server's WebSocket port from the server config file.
     *
     * @return - the WebSocket port
     */
    public static int getWebSocketPort() {
        String webSocketPortString = getPropertyString(ServerProperties.WEBSOCKET_PORT);
        int websocketPort = Integer.parseInt(webSocketPortString);

        return websocketPort;
    }

    /**
     * Gets the Server's IP from the server config file.
     *
     * @return - the IP of the Server
     */
    public static String getServerIp() {
        String serverIp = getPropertyString(ServerProperties.SERVER_IP);

        return serverIp;
    }
}
