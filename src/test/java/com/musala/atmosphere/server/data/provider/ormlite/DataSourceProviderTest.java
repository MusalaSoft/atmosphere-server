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

package com.musala.atmosphere.server.data.provider.ormlite;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.server.data.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.db.ormlite.DeviceDao;
import com.musala.atmosphere.server.data.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DeviceDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;

/**
 *
 * @author filareta.yordanova
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSourceProviderTest {
    @Mock
    private static ServerEventService mockedEventService;

    private static DataSourceProvider dataSourceProvider;

    @BeforeClass
    public static void setUp() {
        dataSourceProvider = new DataSourceProvider();
    }

    @Test
    public void testPublishingEventsForCreatingDaosOnDataSourceInitialized() {
        mockedEventService.publish(new DataSourceInitializedEvent());

        verify(mockedEventService, times(1)).publish(any(AgentDaoCreatedEvent.class));
        verify(mockedEventService, times(1)).publish(any(DeviceDaoCreatedEvent.class));
        verify(mockedEventService, times(1)).publish(any(DevicePoolDaoCreatedEvent.class));
    }

    // FIXME: Because of the monostate pattern the provider is actually shared in all test classes. This makes the test
    // fail if not executed first. See #231
    // @Test
    public void testNotCreatedDaosWhenDataSourceInitializedEventIsMissing() {
        AgentDao agentDao = dataSourceProvider.getAgentDao();
        DeviceDao deviceDao = dataSourceProvider.getDeviceDao();
        DevicePoolDao devicePoolDao = dataSourceProvider.getDevicePoolDao();

        assertNull("Agent data access object was created.", agentDao);
        assertNull("Device data access object was created.", deviceDao);
        assertNull("Device pool data access object was created.", devicePoolDao);
    }
}
