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
 * 
 * Class for all Runtime exceptions that might be thrown from the device pool data access object.
 * 
 * @author filareta.yordanova
 * 
 */
public class DevicePoolDaoRuntimeException extends DaoRuntimeException {
    private static final long serialVersionUID = 2146736971856663286L;

    /**
     * Creates new {@link DevicePoolDaoRuntimeException DevicePoolDaoRuntimeException} with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public DevicePoolDaoRuntimeException(String message) {
        super(message);
    }

    /**
     * Creates new {@link DevicePoolDaoRuntimeException DevicePoolDaoRuntimeException} with the given message and
     * {@link Throwable throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public DevicePoolDaoRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
