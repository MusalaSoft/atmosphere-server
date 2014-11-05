package com.musala.atmosphere.server.data.dao.db.ormlite;

import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.AgentDaoException;
import com.musala.atmosphere.server.dao.exception.DeviceDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoRuntimeException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.data.model.ormilite.Agent;
import com.musala.atmosphere.server.data.model.ormilite.Device;

/**
 * Common class that provides data access object for executing operations with devices and agents from the data source.
 * 
 * @author filareta.yordanova
 * 
 */
public class DevicePoolDao implements IDevicePoolDao {
    private static final Logger LOGGER = Logger.getLogger(DevicePoolDao.class.getSimpleName());

    private DeviceDao deviceDao;

    private AgentDao agentDao;

    public DevicePoolDao(DeviceDao deviceDao, AgentDao agentDao) {
        this.deviceDao = deviceDao;
        this.agentDao = agentDao;
    }

    @Override
    public IDevice addDevice(DeviceInformation deviceInformation, String rmiId, String agentId, long passkey)
        throws DevicePoolDaoException {

        try {
            Agent agent = (Agent) agentDao.selectByAgentId(agentId);
            Device device = new Device(deviceInformation.getSerialNumber(), rmiId, passkey);
            device.setAgent(agent);

            deviceDao.add(device);

            return device;
        } catch (AgentDaoException | DeviceDaoException e) {
            String message = String.format("Adding device with RMI id %s on agent %s failed.", rmiId, agentId);
            throw new DevicePoolDaoException(message, e);
        }
    }

    @Override
    public List<IDevice> getDevices(DeviceParameters parameters) throws DevicePoolDaoException {
        try {
            return deviceDao.filterDevicesByParameters(parameters);
        } catch (DeviceDaoException e) {
            throw new DevicePoolDaoException(e.getMessage(), e);
        }
    }

    @Override
    public IDevice getDevice(String id) throws DevicePoolDaoException {
        try {
            return deviceDao.selectByRmiId(id);
        } catch (DeviceDaoException e) {
            throw new DevicePoolDaoException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasDevice(String id) {
        IDevice selectedDevice;
        try {
            selectedDevice = deviceDao.selectByRmiId(id);
        } catch (DeviceDaoException e) {
            String message = String.format("Device with ID %s was not found.", id);
            LOGGER.error(message, e);
            return false;
        }

        return selectedDevice != null;
    }

    @Override
    public boolean hasDevice(DeviceParameters parameters) {
        try {
            List<IDevice> devices = deviceDao.filterDevicesByParameters(parameters);
            return !devices.isEmpty();
        } catch (DeviceDaoException e) {
            String message = String.format("Device with parameters %s was not found.", parameters);
            LOGGER.error(message, e);
            return false;
        }
    }

    @Override
    public int removeDevices(String agentId) throws DevicePoolDaoException {
        int removedCount = 0;

        Agent agent;

        try {
            agent = (Agent) agentDao.selectByAgentId(agentId);
        } catch (AgentDaoException e) {
            String message = String.format("Failed to remove devices on agent with ID %s.", agentId);
            throw new DevicePoolDaoException(message, e);
        }

        if (agent == null) {
            throw new DevicePoolDaoRuntimeException("You are trying to remove devices on agent, which is actually missing.");
        }

        for (Device device : agent.getDevices()) {
            String deviceId = device.getRmiRegistryId();

            try {
                deviceDao.remove(deviceId);
                removedCount++;
            } catch (DeviceDaoException e) {
                String message = String.format("Failed to remove device with ID %s.", deviceId);
                LOGGER.error(message, e);
            }
        }

        return removedCount;
    }

    @Override
    public void remove(String deviceId) throws DevicePoolDaoException {
        try {
            deviceDao.remove(deviceId);
        } catch (DeviceDaoException e) {
            String message = String.format("Failed to remove device with ID %s.", deviceId);
            throw new DevicePoolDaoException(message, e);
        }
    }

    @Override
    public void update(IDevice device) throws DevicePoolDaoException {
        try {
            deviceDao.update(device);
        } catch (DeviceDaoException e) {
            DeviceInformation deviceInformation = device.getInformation();
            String message = String.format("Failed to device device with serial number %s.",
                                           deviceInformation.getSerialNumber());
            throw new DevicePoolDaoException(message, e);
        }
    }
}
