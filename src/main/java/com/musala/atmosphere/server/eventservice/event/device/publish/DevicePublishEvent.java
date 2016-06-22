package com.musala.atmosphere.server.eventservice.event.device.publish;

import com.musala.atmosphere.server.eventservice.event.device.DeviceEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common ancestor type for publishing and unpublishing devices. These events can be published to an event service and
 * handled from {@link Subscriber subscribers}
 * 
 * @author yavor.stankov
 * 
 */
public interface DevicePublishEvent extends DeviceEvent {

}
