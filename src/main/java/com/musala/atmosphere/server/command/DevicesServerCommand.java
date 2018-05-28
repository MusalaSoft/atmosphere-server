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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelectorBuilder;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.sa.Table;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.Server;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.pool.PoolManager;

/**
 * Prints a list of all devices registered in the pool in a table format.
 *
 * @author dimcho.nedev
 *
 */
public class DevicesServerCommand extends NoParamsServerCommand {
    private static Logger LOGGER = Logger.getLogger(ReleaseCommand.class.getCanonicalName());

    private PoolManager poolManager;

    public DevicesServerCommand(Server server) {
        super(server);
        this.poolManager = PoolManager.getInstance();
    }

    @Override
    protected void executeCommand(String[] params) {
        // Gets a list of all devices (available and allocated)
        List<IDevice> devices = new ArrayList<>();

        DeviceSelector deviceSelector = new DeviceSelectorBuilder().minApi(17).build();
        try {
            List<IDevice> allocatedDevices = poolManager.getDevicePoolDao().getDevices(deviceSelector, true);
            List<IDevice> availableDevices = poolManager.getDevicePoolDao().getDevices(deviceSelector, false);

            devices.addAll(allocatedDevices);
            devices.addAll(availableDevices);
        } catch (DevicePoolDaoException e) {
            LOGGER.error("Faild to get devices from the device pool.", e);
        }

        // Creates and prints the table data
        String[] columnNames = new String[] {"AgentId", "Model", "Serial Number", "MFR", "API", "OS", "CPU", "RAM",
                "DPI", "Resolution", "Emulator", "Tablet", "IsAllocated"};

        String[][] data = new String[devices.size()][columnNames.length];
        for (int i = 0; i < devices.size(); i++) {
            DeviceInformation deviceInformation = devices.get(i).getInformation();

            Pair<Integer, Integer> res = deviceInformation.getResolution();
            data[i][0] = toStr(devices.get(i).getAgentId());
            data[i][1] = toStr(deviceInformation.getModel());
            data[i][2] = toStr(deviceInformation.getSerialNumber());
            data[i][3] = toStr(deviceInformation.getManufacturer());
            data[i][4] = toStr(String.valueOf(deviceInformation.getApiLevel()));
            data[i][5] = toStr(deviceInformation.getOS());
            data[i][6] = toStr(deviceInformation.getCpu());
            data[i][7] = toStr(String.valueOf(deviceInformation.getRam()));
            data[i][8] = toStr(String.valueOf(deviceInformation.getDpi()));
            data[i][9] = toStr(String.format("%sx%s", String.valueOf(res.getKey()), String.valueOf(res.getValue())));
            data[i][10] = toStr(String.valueOf(deviceInformation.isEmulator()));
            data[i][11] = toStr(String.valueOf(deviceInformation.isTablet()));
            data[i][12] = toStr(String.valueOf(devices.get(i).isAllocated()));
        }

        Table table = new Table(columnNames, data);

        table.printTable(new ConsoleControl());
    }

    private String toStr(String data) {
        return data != null ? data : "unknown";
    }

}

