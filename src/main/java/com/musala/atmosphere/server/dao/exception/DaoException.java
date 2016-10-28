package com.musala.atmosphere.server.dao.exception;

/**
 * Common ancestor class for exceptions, which are thrown when operations with data access objects fail.
 * 
 * @author filareta.yordanova
 * 
 */
public abstract class DaoException extends Exception {
    private static final long serialVersionUID = 3949229622228142571L;

    /**
     * Creates new {@link DaoException DaoException} with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Creates new {@link DaoException DaoException} with the given message and {@link Throwable throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public DaoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
