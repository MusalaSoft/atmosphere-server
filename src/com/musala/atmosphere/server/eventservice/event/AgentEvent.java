package com.musala.atmosphere.server.eventservice.event;

import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common ancestor type for all events, related with an agent. These events can be published to an event service and
 * handled from {@link Subscriber subscribers}
 * 
 * @author filareta.yordanova
 * 
 */
public interface AgentEvent extends Event {

}
