package com.musala.atmosphere.server.eventservice.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.eventservice.event.Event;

/**
 * Defines an abstract base class for all objects, that can subscribe to an event service for receiving events.
 * 
 * @author filareta.yordanova
 * 
 */
public abstract class Subscriber {
    private static Logger LOGGER = Logger.getLogger(Subscriber.class.getCanonicalName());

    /**
     * Informs the subscriber when an {@link Event event}, for which the object is subscribed, is received from the
     * event service.
     * 
     * @param event
     *        - event, transferred by the event service.
     */
    public void inform(Event event) {
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = event.getClass();

        // Look for an inform method in the current object
        // that takes the event subtype as a parameter
        try {
            Method method = getClass().getDeclaredMethod("inform", paramTypes);
            Object paramList[] = new Object[1];
            paramList[0] = event;
            method.invoke(this, paramList);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            LOGGER.error("Inform method can not be invoked.", e);
        }

    }
}
