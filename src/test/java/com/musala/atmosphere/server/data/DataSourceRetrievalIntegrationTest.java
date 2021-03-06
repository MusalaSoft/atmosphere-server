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

package com.musala.atmosphere.server.data;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * 
 * @author filareta.yordanova
 * 
 */
public class DataSourceRetrievalIntegrationTest {
    private static ServerEventService eventService;

    private static DataSourceProvider dataSourceProvider;

    private static DataSourceManager dataSourceManager;

    private static AgentDao agentDao;

    private static DevicePoolDao devicePoolDao;

    @BeforeClass
    public static void setUp() {
        dataSourceManager = new DataSourceManager(new DataSourceCallback());
        eventService = new ServerEventService();
        dataSourceProvider = new DataSourceProvider();
        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);
        dataSourceManager.initialize();
    }

    @AfterClass
    public static void tearDownTest() {
        eventService.unsubscribe(DataSourceInitializedEvent.class, null, dataSourceProvider);
    }

    @Test
    public void testAgentDataAccessObjectIsInitializedWhenEventIsPublished() {
        AgentDaoSubscriber subscriber = new AgentDaoSubscriber();
        eventService.subscribe(AgentDaoCreatedEvent.class, subscriber);
        eventService.publish(new AgentDaoCreatedEvent());

        assertNotNull("Expected AgentDao to be initialzied, but received result was null.", agentDao);

        eventService.unsubscribe(AgentDaoCreatedEvent.class, null, subscriber);
    }

    @Test
    public void testDevicePoolDataAccessObjectIsInitializedWhenEventIsPublished() {
        DevicePoolDaoSubscriber subscriber = new DevicePoolDaoSubscriber();
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, subscriber);
        eventService.publish(new DevicePoolDaoCreatedEvent());

        assertNotNull("Expected DevicePoolDao to be initialzied, but received result was null.", devicePoolDao);

        eventService.unsubscribe(DevicePoolDaoCreatedEvent.class, null, subscriber);
    }

    public class AgentDaoSubscriber implements Subscriber {
        public void inform(AgentDaoCreatedEvent event) {
            agentDao = dataSourceProvider.getAgentDao();
        }
    }

    public class DevicePoolDaoSubscriber implements Subscriber {
        public void inform(DevicePoolDaoCreatedEvent event) {
            devicePoolDao = dataSourceProvider.getDevicePoolDao();
        }
    }
}
