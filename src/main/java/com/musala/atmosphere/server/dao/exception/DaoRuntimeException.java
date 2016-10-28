package com.musala.atmosphere.server.dao.exception;

/**
 * Common ancestor class for Runtime exceptions, that might be thrown by data access objects.
 * 
 * 
 * @author filareta.yordanova
 * 
 */
public abstract class DaoRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 8430535042347143386L;

    /**
     * Creates new {@link DaoRuntimeException DaoRuntimeException} with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public DaoRuntimeException(String message) {
        super(message);
    }

    /**
     * Creates new {@link DaoRuntimeException DaoRuntimeException} with the given message and {@link Throwable
     * throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public DaoRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
