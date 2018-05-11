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

package com.musala.atmosphere.server.dao.exception;

/**
 * This exception is thrown when operations with the data access object for the device pool in the data source fail.
 * 
 * @author filareta.yordanova
 * 
 */
public class DevicePoolDaoException extends DaoException {
    private static final long serialVersionUID = 8694197971064213900L;

    /**
     * Creates new {@link DevicePoolDaoException DevicePoolDaoException} with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public DevicePoolDaoException(String message) {
        super(message);
    }

    /**
     * Creates new {@link DevicePoolDaoException DevicePoolDaoException} with the given message and {@link Throwable
     * throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public DevicePoolDaoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
