package com.musala.atmosphere.server.eventservice;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.eventservice.exception.SubscriberMethodInvocationException;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class ServerEventServiceTest {
    private static FakeEvent mockedEvent;

    private static Subscriber mockedSubscriber;

    private static FakeSubscriber fakeSubscriber;

    private static ServerEventService testEventservice;

    @BeforeClass
    public static void setUp() {
        testEventservice = new ServerEventService();
        mockedEvent = new FakeEvent();
        mockedSubscriber = mock(Subscriber.class);
        fakeSubscriber = spy(new FakeSubscriber());
    }

    @Test
    public void testPublishedEventIsReceivedSuccessfully() {
        testEventservice.subscribe(mockedEvent.getClass(), fakeSubscriber);
        testEventservice.publish(mockedEvent);

        verify(fakeSubscriber, times(1)).inform(eq(mockedEvent));
    }

    @Test(expected = SubscriberMethodInvocationException.class)
    public void testReceiverMethodForPublishedEventIsMissing() {
        testEventservice.subscribe(mockedEvent.getClass(), mockedSubscriber);
        testEventservice.publish(mockedEvent);
    }

    public static class FakeSubscriber implements Subscriber {
        public void inform(FakeEvent event) {
        }
    }

    public static class FakeEvent implements Event {
    }
}
