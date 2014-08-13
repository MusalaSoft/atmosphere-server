package com.musala.atmosphere.server.eventservice.subscriber;

import com.musala.atmosphere.server.eventservice.event.Event;

/**
 * Defines an interface for all objects, that can subscribe to an event service for receiving events.
 * 
 * @author filareta.yordanova
 * 
 */
public interface Subscriber {
    /**
     * Informs the subscriber when an {@link Event event}, for which the object is subscribed, is received from the
     * event service.
     * 
     * @param event
     *        - event, transferred by the event service.
     */
    public void inform(Event event);
}
