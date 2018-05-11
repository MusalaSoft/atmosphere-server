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
