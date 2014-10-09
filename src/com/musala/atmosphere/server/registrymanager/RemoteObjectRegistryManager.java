package com.musala.atmosphere.server.registrymanager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.musala.atmosphere.server.eventservice.event.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.eventservice.subscriber.Subscriber;

/**
 * Class which is responsible for publishing remote objects on the server's RMI registry and unpublish remote objects
 * from it.
 * 
 * @author yavor.stankov
 * 
 */
public class RemoteObjectRegistryManager extends Subscriber {
    private static final Logger LOGGER = Logger.getLogger(RemoteObjectRegistryManager.class);

    private static final String DEVICE_RMI_ID_FORMAT = "%s_%s";

    private HashMap<RemoteObject, String> remoteObjectToDeviceRmiIndetifier = new HashMap<RemoteObject, String>();

    private Registry serverRmiRegistry;

    /**
     * Creates a new {@link RemoteObjectRegistryManager} object that use {@link Registry} to manage remote objects.
     * 
     * @param serverRmiRegistry
     *        - RMI registry which will be used for publishing devices
     */
    public RemoteObjectRegistryManager(Registry serverRmiRegistry) {
        this.serverRmiRegistry = serverRmiRegistry;
    }

    /**
     * Publishes a remote object on the server's RMI registry.
     * 
     * @param remoteObject
     *        - the remote object to be published in the registry
     * @param remoteObjectRmiId
     *        - unique remote object identifier.
     * @throws RemoteException
     *         - if failed to publish remote object in the server's RMI registry
     */
    public synchronized void publishObject(RemoteObject remoteObject, String remoteObjectRmiId) throws RemoteException {
        try {
            serverRmiRegistry.rebind(remoteObjectRmiId, remoteObject);

        } catch (RemoteException e) {
            throw new RemoteException("Exception occured when publishing remote object in the server's RMI registry.",
                                      e);
        }

        String message = String.format("Remote object published in the RMI registry under the identifier %s.",
                                       remoteObjectToDeviceRmiIndetifier.get(remoteObject),
                                       "'.");
        LOGGER.info(message);
    }

    /**
     * Unpublishes a remote object from the server's RMI registry.
     * 
     * @param deviceProxyRmiString
     *        - unique remote object identifier
     * @throws RemoteException
     *         - if failed to unpublish the remote object from the server's RMI registry
     * @throws NotBoundException
     */
    public synchronized void unpublishObject(String remoteObjectRmiString) throws RemoteException, NotBoundException {
        try {
            serverRmiRegistry.unbind(remoteObjectRmiString);
        } catch (RemoteException e) {
            throw new RemoteException("Exception occured when unpublishing remote object from the server's RMI registry.",
                                      e);
        }
    }

    /**
     * Informs the {@link RemoteObjectRegistryManager} for {@link DevicePublishedEvent event} received when a device is
     * published to the server's registry.
     * 
     * @param event
     *        - event, which is received when a device is published.
     * @throws RemoteException
     *         - if failed to publish device in the server's RMI registry
     */
    public void inform(DevicePublishedEvent event) throws RemoteException {
        RemoteObject remoteObject = event.getDeviceProxy();

        if (!remoteObjectToDeviceRmiIndetifier.containsKey(remoteObject)) {
            String onAgentId = event.getAgentId();
            String deviceSerialNumber = event.getDeviceSerialNumber();

            String deviceProxyRmiString = buildDeviceRmiIdentifier(onAgentId, deviceSerialNumber);

            publishObject(remoteObject, deviceProxyRmiString);

            remoteObjectToDeviceRmiIndetifier.put(remoteObject, deviceProxyRmiString);
        } else {
            String message = "This device is already in the registry.";
            LOGGER.warn(message);
        }
    }

    /**
     * Informs the {@link RemoteObjectRegistryManager} for {@link DeviceUnpublishedEvent event} received when a device
     * is unpublished from the server's registry.
     * 
     * @param event
     *        - event, which is received when a device is unpublished.
     */
    public void inform(DeviceUnpublishedEvent event) throws RemoteException, NotBoundException {
        RemoteObject remoteObject = event.getUnpublishedDeviceProxy();

        if (remoteObjectToDeviceRmiIndetifier.containsKey(remoteObject)) {
            String deviceProxyRmiString = remoteObjectToDeviceRmiIndetifier.get(remoteObject);
            unpublishObject(deviceProxyRmiString);

            remoteObjectToDeviceRmiIndetifier.remove(remoteObject);
        } else {
            String message = "The device you are trying to unregister is not in the registry.";
            LOGGER.warn(message);
        }
    }

    /**
     * Builds unique device RMI identifier using agent ID and identifier on the Agent for the device wrapper stub.
     * 
     * @param onAgentId
     *        - identifier of the agent on which the device is registered
     * @param deviceWrapperAgentRmiId
     *        - RMI string identifier on the Agent for the device wrapper stub
     * @return - unique device RMI identifier
     */
    private String buildDeviceRmiIdentifier(String onAgentId, String deviceWrapperAgentRmiId) {
        String rmiIdentifier = String.format(DEVICE_RMI_ID_FORMAT, onAgentId, deviceWrapperAgentRmiId);
        return rmiIdentifier;
    }
}
