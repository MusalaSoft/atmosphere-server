package com.musala.atmosphere.server.dao.nativeobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.server.dao.IDevicePoolDao;
import com.musala.atmosphere.server.dao.exception.DevicePoolDaoException;
import com.musala.atmosphere.server.data.model.IDevice;
import com.musala.atmosphere.server.util.DeviceMatchingComparator;

/**
 * A data access object which handles adding, removing, getting and searching for a device/devices.
 * 
 * @author yavor.stankov
 * 
 */
public class DevicePoolDao implements IDevicePoolDao {
    private HashMap<String, IDevice> rmiIdToDevice = new HashMap<String, IDevice>();

    @Override
    public IDevice addDevice(DeviceInformation device, String rmiId, String agentId) {
        Device deviceEntry = new Device(device, rmiId, agentId);
        rmiIdToDevice.put(rmiId, deviceEntry);

        return deviceEntry;
    }

    @Override
    public List<IDevice> getDevices(DeviceParameters parameters) throws DevicePoolDaoException {
        List<IDevice> devicesList = new ArrayList<IDevice>();

        for (IDevice device : rmiIdToDevice.values()) {
            DeviceInformation deviceInformation = device.getInformation();

            if (DeviceMatchingComparator.isValidMatch(parameters, deviceInformation)) {
                devicesList.add(device);
            }
        }

        return devicesList;
    }

    @Override
    public IDevice getDevice(String id) {
        return rmiIdToDevice.get(id);
    }

    @Override
    public boolean hasDevice(String id) {
        return rmiIdToDevice.containsKey(id);
    }

    @Override
    public boolean hasDevice(DeviceParameters parameters) {
        for (IDevice device : rmiIdToDevice.values()) {
            DeviceInformation deviceInformation = device.getInformation();

            if (DeviceMatchingComparator.isValidMatch(parameters, deviceInformation)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int removeDevices(String agentId) {
        int removedCount = 0;

        for (Entry<String, IDevice> idToDevice : rmiIdToDevice.entrySet()) {
            Device device = (Device) idToDevice.getValue();
            if (device.getAgentId().equals(agentId)) {
                String deviceRmiId = idToDevice.getKey();
                rmiIdToDevice.remove(deviceRmiId);
                removedCount++;
            }
        }

        return removedCount;
    }

    @Override
    public void remove(String deviceId) {
        rmiIdToDevice.remove(deviceId);
    }

    @Override
    public void update(IDevice device) throws DevicePoolDaoException {
        // No implementation needed.
    }
}
