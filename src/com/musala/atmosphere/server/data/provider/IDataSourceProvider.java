package com.musala.atmosphere.server.data.provider;

import com.musala.atmosphere.server.dao.IAgentDao;
import com.musala.atmosphere.server.dao.IDeviceDao;
import com.musala.atmosphere.server.dao.IDevicePoolDao;

/**
 * Common interface responsible for providing data access objects to operate on the data source.
 * 
 * @author filareta.yordanova
 * 
 */
public interface IDataSourceProvider {
    /**
     * Gets data access object for modifying agents in the data source.
     * 
     * @return agent data access object
     */
    public IAgentDao getAgentDao();

    /**
     * Gets data access object for modifying devices in the data source.
     * 
     * @return device data access object
     */
    public IDeviceDao getDeviceDao();

    /**
     * Gets data access object for maintaining the device pool.
     * 
     * @return device pool data access object
     */
    public IDevicePoolDao getDevicePoolDao();

}
