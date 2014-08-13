package com.musala.atmosphere.server.eventservice;

import com.musala.atmosphere.server.eventservice.filter.Filter;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Common class, that holds information about a single subscription - subscriber, filter and event type, used by the
 * event service for event filtering and fulfilling subscriber's criteria.
 * 
 * @author filareta.yordanova
 * 
 */

public class Subscription {
    private Class<?> eventType;

    private Filter filter;

    private Subscriber subscriber;

    public Subscription(Class<?> eventType, Filter filter, Subscriber subscriber) {
        this.eventType = eventType;
        this.filter = filter;
        this.subscriber = subscriber;
    }

    /**
     * Gets the type of the event for this subscription.
     * 
     * @return type of the event.
     */
    public Class<?> getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event for this subscription.
     * 
     * @param eventType
     *        - type of the event.
     */
    public void setEventType(Class<?> eventType) {
        this.eventType = eventType;
    }

    /**
     * Gets the event filter for this subscription.
     * 
     * @return event filter for the subscription.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the event filter for this subscription.
     * 
     * @param filter
     *        - the event filter for this subscription
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Gets the subscriber for the event type and filter in this subscription.
     * 
     * @return the subscriber for this subscription.
     */
    public Subscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Sets the subscriber for the event type and filter in this subscription.
     * 
     * @param subscriber
     *        - the subscriber for the this subscription.
     */
    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }
}
