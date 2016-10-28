package com.musala.atmosphere.server.eventservice.event.device;

import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common ancestor type for all events, related with a device. These events can be published to an event service and
 * handled from {@link Subscriber subscribers}
 * 
 * @author yavor.stankov
 * 
 */
public interface DeviceEvent extends Event {

}
