package com.musala.atmosphere.server.data.provider.nativeprovider;

import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.nativeobject.AgentDao;
import com.musala.atmosphere.server.dao.nativeobject.DevicePoolDao;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;

/**
 * Class which creates data access objects for agent, and device pool and publishes events to the
 * {@link ServerEventService event service} after each Dao is created
 * 
 * @author yavor.stankov
 * 
 */
public class DataSourceProvider implements IDataSourceProvider {

    private static IDevicePoolDao devicePoolDao = new DevicePoolDao();

    private static IAgentDao agentDao = new AgentDao();

    private static ServerEventService eventService = new ServerEventService();

    @Override
    public IAgentDao getAgentDao() {
        return agentDao;
    }

    @Override
    public IDevicePoolDao getDevicePoolDao() {
        return devicePoolDao;
    }

    /**
     * Informs data source provider for {@link DataSourceInitializedEvent event} received when data source is
     * initialized.
     * 
     * @param event
     *        - event, which is received when data source is initialized
     */
    public void inform(DataSourceInitializedEvent event) {
        eventService.publish(new DevicePoolDaoCreatedEvent());
        eventService.publish(new AgentDaoCreatedEvent());
    }
}
