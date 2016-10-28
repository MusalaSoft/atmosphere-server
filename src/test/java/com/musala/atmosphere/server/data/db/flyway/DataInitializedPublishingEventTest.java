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
