package com.musala.atmosphere.server.eventservice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.eventservice.filter.Filter;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Class, which brokers events between subscriber and publisher. The service manages events, received and published on
 * the server.
 * 
 * @author filareta.yordanova
 * 
 */
public class ServerEventService {
    private static Logger LOGGER = Logger.getLogger(ServerEventService.class);

    private static Set<Subscription> subscriptions = Collections.synchronizedSet(new HashSet<Subscription>());

    private static Class<?> eventClass = Event.class;

    /**
     * Publishes an event to the server event service, that is managed and sent to the subscribers.
     * 
     * @param event
     *        - event to be published.
     */
    public synchronized void publish(Event event) {
        String publishMessage = String.format("Publishing event %s in thread %s.",
                                            event.getClass().getSimpleName(),
                                            Thread.currentThread().getName());
        LOGGER.debug(publishMessage);

        for (Subscription subscription : subscriptions) {
            Filter subscriptionFilter = subscription.getFilter();
            Class<?> subscriptionEventType = subscription.getEventType();

            Class<?> publishedEventClass = event.getClass();
            boolean isEventCompatible = subscriptionEventType.isAssignableFrom(publishedEventClass);
            boolean isFilterApplicabale = subscriptionFilter == null || subscriptionFilter.apply(event);

            if (isEventCompatible && isFilterApplicabale) {
                Subscriber subscriber = subscription.getSubscriber();
                subscriber.inform(event);
            }
        }
    }

    /**
     * Adds a subscription on the event service for the requested subscriber for events from the given type and matching
     * the filter.
     * 
     * @param eventType
     *        - type of the published event.
     * @param filter
     *        - matching criteria for the corresponding eventType.
     * @param subscriber
     *        - object, subscribed for the given eventType and filter.
     */
    public void subscribe(Class<?> eventType, Filter filter, Subscriber subscriber) {
        if (!eventClass.isAssignableFrom(eventType)) {
            return;
        }

        String subscribeMessage = String.format("%s subscribes for %s.",
                                            subscriber.getClass().getSimpleName(),
                                            eventType.getSimpleName());
        LOGGER.debug(subscribeMessage);
        Subscription subscription = new Subscription(eventType, filter, subscriber);
        synchronized (subscriptions) {
            if (!subscriptions.contains(subscription)) {
                subscriptions.add(subscription);
            }
        }
    }

    /**
     * Removes the subscription from the event service for the requested subscriber for events from the given type and
     * matching the filter.
     * 
     * @param eventType
     *        - type of the published event.
     * @param filter
     *        - matching criteria for the corresponding eventType.
     * @param subscriber
     *        - object, which is unsubscribed for the given eventType and filter.
     */
    public void unsubscribe(Class<?> eventType, Filter filter, Subscriber subscriber) {
        if (!eventClass.isAssignableFrom(eventType)) {
            return;
        }

        String unsubscribeMessage = String.format("%s unsubscribes for %s.",
                                            subscriber.getClass().getSimpleName(),
                                            eventType.getSimpleName());
        LOGGER.debug(unsubscribeMessage);
        Subscription subscription = new Subscription(eventType, filter, subscriber);
        subscriptions.remove(subscription);
    }
}