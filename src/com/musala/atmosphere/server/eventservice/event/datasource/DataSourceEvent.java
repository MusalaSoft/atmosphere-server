package com.musala.atmosphere.server.eventservice.event.datasource;

import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common ancestor type for all events, related with data operations. These events can be published to an event
 * service and handled from {@link Subscriber subscribers}
 * 
 * @author filareta.yordanova
 * 
 */
public interface DataSourceEvent extends Event {
}
