package com.musala.atmosphere.server.dao.nativeobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.server.dao.IDeviceDAO;
import com.musala.atmosphere.server.dao.IDevicePoolDAO;
import com.musala.atmosphere.server.util.DeviceMatchingComparator;

/**
 * A data access object which handles adding, removing, getting and searching for a device/devices.
 * 
 * @author yavor.stankov
 * 
 */
public class DevicePoolDao implements IDevicePoolDAO {
    private HashMap<String, IDeviceDAO> rmiIdToDeviceDAO = new HashMap<String, IDeviceDAO>();

    @Override
    public IDeviceDAO addDevice(DeviceInformation device, String rmiId, String agentId) {
        DeviceDao deviceEntry = new DeviceDao(device, rmiId, agentId);
        rmiIdToDeviceDAO.put(rmiId, deviceEntry);

        return deviceEntry;
    }

    @Override
    public List<IDeviceDAO> getDevices(DeviceParameters parameters) {
        List<IDeviceDAO> devicesList = new ArrayList<IDeviceDAO>();

        for (IDeviceDAO deviceDao : rmiIdToDeviceDAO.values()) {
            DeviceInformation deviceInformation = deviceDao.getInformation();

            if (!DeviceMatchingComparator.isValidMatch(parameters, deviceInformation)) {
                devicesList.add(deviceDao);
            }
        }

        return devicesList;
    }

    @Override
    public IDeviceDAO getDevice(String id) {
        return rmiIdToDeviceDAO.get(id);
    }

    @Override
    public boolean hasDevice(String id) {
        return rmiIdToDeviceDAO.containsKey(id);
    }

    @Override
    public boolean hasDevice(DeviceParameters parameters) {
        for (IDeviceDAO deviceDao : rmiIdToDeviceDAO.values()) {
            DeviceInformation deviceInformation = deviceDao.getInformation();

            if (!DeviceMatchingComparator.isValidMatch(parameters, deviceInformation)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeDeivces(String agentId) {
        for (Entry<String, IDeviceDAO> idToDeviceDao : rmiIdToDeviceDAO.entrySet()) {
            DeviceDao deviceDAO = (DeviceDao) idToDeviceDao.getValue();
            if (deviceDAO.getAgentId() == agentId) {
                String deviceRmiId = idToDeviceDao.getKey();
                rmiIdToDeviceDAO.remove(deviceRmiId);
            }
        }

        return true;
    }

    @Override
    public boolean remove(String id) {
        rmiIdToDeviceDAO.remove(id);

        return true;
    }
}
