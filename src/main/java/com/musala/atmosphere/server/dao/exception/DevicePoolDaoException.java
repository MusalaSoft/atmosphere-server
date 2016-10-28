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
