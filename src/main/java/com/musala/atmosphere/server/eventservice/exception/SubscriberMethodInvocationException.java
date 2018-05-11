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

package com.musala.atmosphere.server.eventservice.exception;

import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * An {@link java.lang.RuntimeException exception} thrown when informing {@link Subscriber subscriber} for certain
 * {@link com.musala.atmosphere.server.eventservice.event.Event event} fails.
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
