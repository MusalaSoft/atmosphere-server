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

package com.musala.atmosphere.server.data.db.flyway;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;

/**
 * 
 * @author filareta.yordanova
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DataInitializedPublishingEventTest {
    private static String EXPECTED_PUBLISHED_EVENT_CLASS_NAME = DataSourceInitializedEvent.class.getSimpleName();

    private static DataSourceManager dataSourceManager;

    @InjectMocks
    private static DataSourceCallback dataSourceCallback;

    @Mock
    private static ServerEventService mockedEventService;

    @BeforeClass
    public static void setUp() {
        dataSourceCallback = new DataSourceCallback();
        dataSourceManager = new DataSourceManager(dataSourceCallback);
    }

    @Test
    public void testCorrectEventTypeReceivedOnDataInitialized() {
        dataSourceManager.initialize();

        ArgumentCaptor<DataSourceInitializedEvent> argument = ArgumentCaptor.forClass(DataSourceInitializedEvent.class);
        verify(mockedEventService).publish(argument.capture());

        String publishedEventClassName = argument.getValue().getClass().getSimpleName();

        assertEquals("Data source initialized event was not published.",
                     EXPECTED_PUBLISHED_EVENT_CLASS_NAME,
                     publishedEventClassName);
    }
}
