package com.musala.atmosphere.server.data.db.flyway;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.musala.atmosphere.server.eventservice.ServerEventService;

/**
 * 
 * @author filareta.yordanova
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseInitializationTest {
    @InjectMocks
    private static DataSourceManager dataSourceManager;

    @Mock
    private static Flyway mockedFlywayInstance;

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
    public void testMigrationsRunSuccessfully() {
        dataSourceManager.initialize();

        verify(mockedFlywayInstance, times(1)).setCallbacks(dataSourceCallback);
        verify(mockedFlywayInstance, times(1)).setDataSource(any(String.class), any(String.class), any(String.class));
        verify(mockedFlywayInstance, times(1)).setLocations(any(String.class));
        verify(mockedFlywayInstance, times(1)).migrate();
    }
}
