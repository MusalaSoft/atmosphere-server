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

package com.musala.atmosphere.server.eventservice;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(subscriber).append(filter).append(eventType).toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null || !getClass().equals(object.getClass())) {
            return false;
        }

        Subscription subscription = (Subscription) object;

        return new EqualsBuilder().append(subscriber, subscription.subscriber)
                                  .append(filter, subscription.filter)
                                  .append(eventType, subscription.eventType)
                                  .isEquals();
    }
}
