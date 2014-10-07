package com.musala.atmosphere.server.eventservice;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.DeviceProxy;

/**
 * Class which is responsible for publishing devices on the server's RMI registry.
 * 
 * @author yavor.stankov
 * 
 */
public class RmiRegistryManager {

    private static Logger LOGGER = Logger.getLogger(RmiRegistryManager.class);

    private Registry serverRmiRegistry;

    /**
     * Creates a new {@link RmiRegistryManager} object that use {@link Registry} to manage remote objects.
     * 
     * @param serverRmiRegistry
     *        - RMI registry which will be used for publishing devices
     */
    public RmiRegistryManager(Registry serverRmiRegistry) {
        this.serverRmiRegistry = serverRmiRegistry;
    }

    /**
     * Publishes a device on the server's RMI registry.
     * 
     * @param deviceProxy
     *        - the remote device object to be published in the registry
     * @param deviceWrapperAgentRmiId
     *        - RMI string identifier on the Agent for the device wrapper stub
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     * @throws RemoteException
     *         - if failed to publish device in the server's RMI registry
     */
    public void publishDevice(DeviceProxy deviceProxy, String deviceWrapperAgentRmiId, String onAgentId)
        throws RemoteException {

        String deviceProxyRmiString = buildDeviceRmiIdentifier(onAgentId, deviceWrapperAgentRmiId);

        try {
            serverRmiRegistry.rebind(deviceProxyRmiString, deviceProxy);
        } catch (RemoteException e) {
            throw new RemoteException("Exception occured when publishing device in the server's RMI registry.", e);
        }

        String message = String.format("Device published in the RMI registry under the identifier %s.",
                                       deviceProxyRmiString,
                                       "'.");
        LOGGER.info(message);
    }

    /**
     * Builds unique device RMI identifier using agent ID and identifier on the Agent for the device wrapper stub.
     * 
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     * @param deviceWrapperAgentRmiId
     *        - RMI string identifier on the Agent for the device wrapper stub
     * @return - unique deviceRMI identifier
     */
    public String buildDeviceRmiIdentifier(String onAgentId, String deviceWrapperAgentRmiId) {
        String rmiIdentifier = String.format("%s_%s", onAgentId, deviceWrapperAgentRmiId);
        return rmiIdentifier;
    }
}
