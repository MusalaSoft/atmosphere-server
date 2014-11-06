package com.musala.atmosphere.server.eventservice.exception;

import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * An {@link RuntimeException exception} thrown when informing {@link Subscriber subscriber} for certain {@link Event
 * event} fails.
 * 
 * @author filareta.yordanova
 * 
 */
public class SubscriberMethodInvocationException extends RuntimeException {
    private static final long serialVersionUID = 3325987867240575134L;

    /**
     * Creates new exception with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public SubscriberMethodInvocationException(String message) {
        super(message);
    }

    /**
     * Creates new exception with the given message and {@link Throwable throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public SubscriberMethodInvocationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
