package com.musala.atmosphere.server.data.fake;

import com.musala.atmosphere.server.data.IDataSourceManager;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;

/**
 * Class that fakes the management of the operations with the data source.
 * 
 * @author yavor.stankov
 * 
 */
public class DataSourceManager implements IDataSourceManager {
    ServerEventService eventService = new ServerEventService();

    @Override
    public void initialize() {
        eventService.publish(new DataSourceInitializedEvent());
    }
}
