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

package com.musala.atmosphere.server.command;

import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.Server;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Releases the allocated devices - release all devices and release by serial number.
 * 
 * @author dimcho.nedev
 *
 */
public class ReleaseCommand extends ParamsServerCommand {
    private static Logger LOGGER = Logger.getLogger(ReleaseCommand.class.getCanonicalName());

    private PoolManager poolManager;
    private ClientRequestMonitor monitor;

    public ReleaseCommand(Server server) {
        super(server);
        this.poolManager = PoolManager.getInstance();
        this.monitor = new ClientRequestMonitor();
    }

    @Override
    protected void executeCommand(String[] params) {
        List<String> deviceList = poolManager.getAllAllocatedDevices();

        if (deviceList.isEmpty()) {
            server.writeLineToConsole("No device to release.");
            return;
        }

        for (String deviceId : deviceList) {
            if (params[0].equals("all") || deviceId.endsWith(params[0])) {
                try {
                    poolManager.releaseDevice(deviceId);
                    monitor.unregisterDevice(deviceId);
                    server.writeLineToConsole(String.format("Device with ID: %s released.", deviceId));
                } catch (DevicePoolDaoException e) {
                    LOGGER.error(String.format("Failed to release a device with ID %s", deviceId), e);
                }
            }
        }
    }

}
