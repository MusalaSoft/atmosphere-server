package com.musala.atmosphere.server.data.provider.ormlite;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.musala.atmosphere.server.data.dao.db.ormlite.AgentDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.DeviceDao;
import com.musala.atmosphere.server.data.dao.db.ormlite.DevicePoolDao;
import com.musala.atmosphere.server.data.db.constant.Property;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.AgentDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DeviceDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;

/**
 * Class which creates data access objects for agent, device and device pool and publishes events to the
 * {@link ServerEventService event service} after each Dao is created.
 * 
 * @author filareta.yordanova
 * 
 */
public class DataSourceProvider implements IDataSourceProvider {
    private static final Logger LOGGER = Logger.getLogger(DataSourceProvider.class);

    private static AgentDao wrappedAgentDao = null;

    private static DeviceDao wrappedDeviceDao = null;

    private static DevicePoolDao devicePoolDao = null;

    private static ServerEventService eventService = new ServerEventService();

    private static ConnectionSource connectionSource = null;

    // TODO: Use a better fix in the future.
    @SuppressWarnings("unused")
    private static DatabaseConnection keepAliveConnection = null;

    @Override
    public AgentDao getAgentDao() {
        return wrappedAgentDao;
    }

    @Override
    public DevicePoolDao getDevicePoolDao() {
        return devicePoolDao;
    }

    /**
     * Gets data access object for modifying devices in the data source.
     * 
     * @return a {@link DeviceDao device data access object}
     */
    public DeviceDao getDeviceDao() {
        return wrappedDeviceDao;
    }

    /**
     * Informs data source provider for {@link DataSourceInitializedEvent event} received when data source is
     * initialized.
     * 
     * @param event
     *        - event, which is received when data source is initialized
     */
    public void inform(DataSourceInitializedEvent event) {
        // TODO: Data AccessObjects can be passed through events.
        try {
            if (connectionSource == null) {
                connectionSource = new JdbcConnectionSource(Property.DATABASE_URL);
                keepAliveConnection = connectionSource.getReadOnlyConnection();
            }

            if (wrappedAgentDao == null) {
                Dao<Agent, String> agentDao = DaoManager.createDao(connectionSource, Agent.class);
                wrappedAgentDao = new AgentDao(agentDao);

                publishDataSourceCreatedEvent(new AgentDaoCreatedEvent());
            }

            if (wrappedDeviceDao == null) {
                Dao<Device, String> deviceDao = DaoManager.createDao(connectionSource, Device.class);
                wrappedDeviceDao = new DeviceDao(deviceDao);

                publishDataSourceCreatedEvent(new DeviceDaoCreatedEvent());
            }

            if (devicePoolDao == null) {
                devicePoolDao = new DevicePoolDao(wrappedDeviceDao, wrappedAgentDao);

                publishDataSourceCreatedEvent(new DevicePoolDaoCreatedEvent());
            }
        } catch (SQLException e) {
            LOGGER.error("Connection to the data source failed.", e);
        }
    }

    private void publishDataSourceCreatedEvent(DataSourceCreatedEvent dataSourceCreatedEvent) {
        eventService.publish(dataSourceCreatedEvent);
    }
}
