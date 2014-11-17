package com.musala.atmosphere.server.data.provider.ormlite;

import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.j256.ormlite.support.ConnectionSource;
import com.musala.atmosphere.server.data.dao.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.DeviceDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.DevicePoolDao;
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

    @Mock
    private static ConnectionSource mockedConnectionSource;

    @Mock
    private static AgentDao mockedAgentDao;

    @Mock
    private static DeviceDao mockedDeviceDao;

    @Mock
    private static DevicePoolDao mockedDevicePoolDao;

    @InjectMocks
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

    @Test
    public void testNotCreatedDaosWhenDataSourceInitializedEventIsMissing() {
        AgentDao agentDao = dataSourceProvider.getAgentDao();
        DeviceDao deviceDao = dataSourceProvider.getDeviceDao();
        DevicePoolDao devicePoolDao = dataSourceProvider.getDevicePoolDao();

        assertNull("Agent data access object was created.", agentDao);
        assertNull("Device data access object was created.", deviceDao);
        assertNull("Device pool data access object was created.", devicePoolDao);
    }
}
