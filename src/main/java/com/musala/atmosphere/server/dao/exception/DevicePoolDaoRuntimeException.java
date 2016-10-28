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
