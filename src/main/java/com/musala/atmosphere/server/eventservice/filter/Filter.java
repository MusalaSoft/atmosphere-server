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

package com.musala.atmosphere.server.eventservice.filter;

import com.musala.atmosphere.server.eventservice.event.Event;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * A common abstract class, responsible for discarding {@link Event events} not of interest to a {@link Subscriber
 * subscriber}.
 * 
 * @author filareta.yordanova
 * 
 */
public abstract class Filter {
    /**
     * Checks whether the event matches all the criteria added in the filter.
     * 
     * @param event
     *        - event, which will be checked.
     * @return <code>true</code> if the event matches the criteria in the created filter, <code>false</code> otherwise.
     */
    public abstract boolean apply(Event event);
}
