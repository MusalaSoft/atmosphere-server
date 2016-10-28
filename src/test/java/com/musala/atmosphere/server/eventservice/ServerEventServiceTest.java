package com.musala.atmosphere.server.eventservice;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
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
        mockedSubscriber = mock(Subscriber.class);
        fakeSubscriber = spy(new FakeSubscriber());
    }

    @Before
    public void setUpTest() {
        mockedEvent = new FakeEvent();
        testEventservice.unsubscribe(FakeEvent.class, null, mockedSubscriber);
        testEventservice.unsubscribe(FakeEvent.class, null, fakeSubscriber);
    }

    @Test
    public void testPublishedEventIsReceivedSuccessfully() {
        testEventservice.subscribe(FakeEvent.class, fakeSubscriber);
        testEventservice.publish(mockedEvent);

        verify(fakeSubscriber, times(1)).inform(eq(mockedEvent));
    }

    @Test(expected = SubscriberMethodInvocationException.class)
    public void testReceiverMethodForPublishedEventIsMissing() {
        testEventservice.subscribe(FakeEvent.class, mockedSubscriber);
        testEventservice.publish(mockedEvent);
    }

    public class FakeEvent implements Event {
    }

    public static class FakeSubscriber implements Subscriber {
        public void inform(FakeEvent event) {
        }
    }
}
