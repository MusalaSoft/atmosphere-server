package com.musala.atmosphere.server.eventservice.event.device.allocate;

import com.musala.atmosphere.server.eventservice.event.device.DeviceEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common ancestor type for allocating and releasing a device. These events can be published to an event service and
 * handled from {@link Subscriber subscribers}
 * 
 * @author yavor.stankov
 * 
 */
public interface DeviceAllocateEvent extends DeviceEvent {

}
