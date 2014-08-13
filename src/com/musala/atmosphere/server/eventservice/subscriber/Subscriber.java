package com.musala.atmosphere.server.eventservice.subscriber;

import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.Event;

/**
 * Defines an interface for all objects, that can subscribe to an {@link ServerEventService event service} for receiving
 * events.
 * 
 * @author filareta.yordanova
 * 
 */
public interface Subscriber {
    /**
     * Informs the subscriber when an {@link Event event}, for which the object is subscribed, is received from the
     * {@link ServerEventService event service}.
     * 
     * @param event
     *        - event, transferred by the event service.
     */
    public void inform(Event event);
}
