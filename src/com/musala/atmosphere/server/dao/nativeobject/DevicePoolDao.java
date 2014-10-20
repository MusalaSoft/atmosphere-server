package com.musala.atmosphere.server.dao.nativeobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.server.dao.IDeviceDao;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.util.DeviceMatchingComparator;

/**
 * A data access object which handles adding, removing, getting and searching for a device/devices.
 * 
 * @author yavor.stankov
 * 
 */
public class DevicePoolDao implements IDevicePoolDao {
    private HashMap<String, IDeviceDao> rmiIdToDeviceDao = new HashMap<String, IDeviceDao>();

    @Override
    public IDeviceDao addDevice(DeviceInformation device, String rmiId, String agentId) {
        DeviceDao deviceEntry = new DeviceDao(device, rmiId, agentId);
        rmiIdToDeviceDao.put(rmiId, deviceEntry);

        return deviceEntry;
    }

    @Override
    public List<IDeviceDao> getDevices(DeviceParameters parameters) {
        List<IDeviceDao> devicesList = new ArrayList<IDeviceDao>();

        for (IDeviceDao deviceDao : rmiIdToDeviceDao.values()) {
            DeviceInformation deviceInformation = deviceDao.getInformation();

            if (DeviceMatchingComparator.isValidMatch(parameters, deviceInformation)) {
                devicesList.add(deviceDao);
            }
        }

        return devicesList;
    }

    @Override
    public IDeviceDao getDevice(String id) {
        return rmiIdToDeviceDao.get(id);
    }

    @Override
    public boolean hasDevice(String id) {
        return rmiIdToDeviceDao.containsKey(id);
    }

    @Override
    public boolean hasDevice(DeviceParameters parameters) {
        for (IDeviceDao deviceDao : rmiIdToDeviceDao.values()) {
            DeviceInformation deviceInformation = deviceDao.getInformation();

            if (!DeviceMatchingComparator.isValidMatch(parameters, deviceInformation)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeDeivces(String agentId) {
        for (Entry<String, IDeviceDao> idToDeviceDao : rmiIdToDeviceDao.entrySet()) {
            DeviceDao deviceDAO = (DeviceDao) idToDeviceDao.getValue();
            if (deviceDAO.getAgentId() == agentId) {
                String deviceRmiId = idToDeviceDao.getKey();
                rmiIdToDeviceDao.remove(deviceRmiId);
            }
        }

        return true;
    }

    @Override
    public boolean remove(String id) {
        rmiIdToDeviceDao.remove(id);

        return true;
    }
}
