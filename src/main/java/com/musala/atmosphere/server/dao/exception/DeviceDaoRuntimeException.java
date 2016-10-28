package com.musala.atmosphere.server.dao.exception;

/**
 * 
 * Class for all Runtime exceptions that might be thrown from the device data access object.
 * 
 * @author filareta.yordanova
 * 
 */
public class DeviceDaoRuntimeException extends DaoRuntimeException {
    private static final long serialVersionUID = 6611522373486459831L;

    /**
     * Creates new {@link DeviceDaoRuntimeException DeviceDaoRuntimeException} with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public DeviceDaoRuntimeException(String message) {
        super(message);
    }

    /**
     * Creates new {@link DeviceDaoRuntimeException DeviceDaoRuntimeException} with the given message and
     * {@link Throwable throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public DeviceDaoRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
