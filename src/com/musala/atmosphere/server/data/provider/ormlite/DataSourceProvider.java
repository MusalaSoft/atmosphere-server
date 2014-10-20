package com.musala.atmosphere.server.data.provider.ormlite;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.IDeviceDao;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.db.constant.Property;
import com.musala.atmosphere.server.data.model.Agent;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DeviceDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Class which creates data access objects for agent, device and device pool and publishes events to the
 * {@link ServerEventService event service} after each Dao is created.
 * 
 * @author filareta.yordanova
 * 
 */
public class DataSourceProvider extends Subscriber implements IDataSourceProvider {
    private static final Logger LOGGER = Logger.getLogger(DataSourceProvider.class);

    private static IAgentDao wrappedAgentDao = null;

    private static IDeviceDao wrappedDeviceDao = null;

    private static IDevicePoolDao wrappedDevicePoolDao = null;

    private static ServerEventService eventService = new ServerEventService();

    private static ConnectionSource connectionSource = null;

    @Override
    public IAgentDao getAgentDao() {
        return wrappedAgentDao;
    }

    @Override
    public IDeviceDao getDeviceDao() {
        return wrappedDeviceDao;
    }

    @Override
    public IDevicePoolDao getDevicePoolDao() {
        return wrappedDevicePoolDao;
    }

    /**
     * Informs data source provider for {@link DataSourceInitializedEvent event} received when data source is
     * initialized.
     * 
     * @param event
     *        - event, which is received when data source is initialized
     */
    public void inform(DataSourceInitializedEvent event) {
        try {
            if (connectionSource == null) {
                connectionSource = new JdbcConnectionSource(Property.DATABASE_URL);
            }

            // TODO: Initialize data access objects here, when the provided interfaces are implemented.

            if (wrappedAgentDao == null) {
                Dao<Agent, String> agentDao = DaoManager.createDao(connectionSource, Agent.class);
                wrappedAgentDao = new AgentDao(agentDao);

                publishDataSourceCreatedEvent(new AgentDaoCreatedEvent());
            }

            // TODO: Needs to be re-factored after adding initialization of the data access objects.
            publishDataSourceCreatedEvent(new DeviceDaoCreatedEvent());
            publishDataSourceCreatedEvent(new DevicePoolDaoCreatedEvent());
        } catch (SQLException e) {
            LOGGER.error("Connection to the data source failed.", e);
        }
    }

    private void publishDataSourceCreatedEvent(DataSourceCreatedEvent dataSourceCreatedEvent) {
        eventService.publish(dataSourceCreatedEvent);
    }
}
