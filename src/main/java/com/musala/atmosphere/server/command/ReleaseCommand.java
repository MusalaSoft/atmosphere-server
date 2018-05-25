package com.musala.atmosphere.server.command;

import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.Server;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
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

    public ReleaseCommand(Server server) {
        super(server);
        this.poolManager = PoolManager.getInstance();
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
                    // System.out.println(deviceId);
                    server.writeLineToConsole(String.format("Device with ID: %s released.", deviceId));
                } catch (DevicePoolDaoException e) {
                    LOGGER.error(String.format("Failed to release a device with ID %s", deviceId), e);
                }
            }
        }
    }

}
