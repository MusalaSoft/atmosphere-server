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
import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.IDeviceDao;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.DeviceDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.DevicePoolDaoCreatedEvent;

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
    private static IAgentDao mockedAgentDao;

    @Mock
    private static IDeviceDao mockedDeviceDao;

    @Mock
    private static IDevicePoolDao mockedDevicePoolDao;

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
        IAgentDao agentDao = dataSourceProvider.getAgentDao();
        IDeviceDao deviceDao = dataSourceProvider.getDeviceDao();
        IDevicePoolDao devicePoolDao = dataSourceProvider.getDevicePoolDao();

        assertNull("Agent data access object was created.", agentDao);
        assertNull("Device data access object was created.", deviceDao);
        assertNull("Device pool data access object was created.", devicePoolDao);
    }
}
