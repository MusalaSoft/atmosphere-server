package com.musala.atmosphere.server.eventservice.filter;

import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common abstract class, responsible for discarding {@link Event events} not of interest to a {@link Subscriber
 * subscriber}.
 * 
 * @author filareta.yordanova
 * 
 */
public abstract class Filter {
    /**
     * Checks whether the event matches all the criteria added in the filter.
     * 
     * @param event
     *        - event, which will be checked.
     * @return <code>true</code> if the event matches the criteria in the created filter, <code>false</code> otherwise.
     */
    public abstract boolean apply(Event event);
}
