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

package com.musala.atmosphere.server;

import java.util.Random;

import com.musala.atmosphere.commons.cs.exception.InvalidPasskeyException;
import com.musala.atmosphere.commons.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;

/**
 * Class whose purpose is to generate and validate passkeys.
 *
 * @author yavor.stankov
 *
 */
public class PasskeyAuthority {
    private static final Random generator = new Random();

    private static IDataSourceProvider dataSourceProvider = new DataSourceProvider();

    /**
     * Generates new passkey.
     *
     * @return the newly generated passkey
     */
    public static long generatePasskey() {
        return generatePasskey(0);
    }

    /**
     * Generates new passkey, different from the device's current passkey.
     *
     * @param oldPasskey
     *        - the device's current passkey.
     * @return the newly generated passkey.
     */
    public static long generatePasskey(long oldPasskey) {
        long newPasskey = 0;
        do {
            long initialRandomValue = generator.nextLong();
            Random seededGenerator = new Random(initialRandomValue);
            long secondRandomValue = seededGenerator.nextLong();
            newPasskey = initialRandomValue ^ secondRandomValue;
        } while (newPasskey == oldPasskey);

        return newPasskey;
    }

    /**
     * Validates the passkey for the given device.
     *
     * @param invocationPasskey
     *        - the passkey that the device should have
     * @param deviceId
     *        - the unique identifier of the device whose passkey must be validated
     * @throws InvalidPasskeyException
     *         if the passed passkey is not valid
     * @throws DeviceNotFoundException
     *         thrown when an action fails, because the server fails to find the target device
     */
    public static void validatePasskey(long invocationPasskey, String deviceId)
        throws InvalidPasskeyException,
            DeviceNotFoundException {
        IDevicePoolDao devicePoolDao = dataSourceProvider.getDevicePoolDao();
        IDevice device = null;

        try {
            device = devicePoolDao.getDevice(deviceId);
        } catch (DevicePoolDaoException e) {
            String message = "Failed to find the requested device for validation.";
            throw new DeviceNotFoundException(message);
        }

        long passkey = device.getPasskey();

        if (passkey != invocationPasskey) {
            throw new InvalidPasskeyException("The passkey is not valid for the specified device.");
        }
    }
}
