package com.musala.atmosphere.server.monitor.ping;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IServerEventSender;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.AgentDisconnectedEvent;

/**
 * Class that periodically sends ping requests to an agent to determine whether an agent is still connected or must be
 * unregistered.
 * 
 * @author filareta.yordanova
 * 
 */
public class PingRequestHandler implements Runnable {
    private static Logger LOGGER = Logger.getLogger(PingRequestHandler.class.getCanonicalName());

    private static final long PING_INTERVAL = 5000;

    private IServerEventSender serverEventSender;

    private String agentId;

    private IAgentManager agentManager;

    private ServerEventService eventService;

    private volatile boolean isRunning;

    public PingRequestHandler(IAgentManager agentManager, Registry agentRegistry) {
        this.agentManager = agentManager;
        eventService = new ServerEventService();
        isRunning = true;

        try {
            serverEventSender = (IServerEventSender) agentRegistry.lookup(RmiStringConstants.AGENT_EVENT_RECEIVER.toString());
            agentId = agentManager.getAgentId();
        } catch (RemoteException | NotBoundException e) {
            publishEventOnAgentDisconnected();
            terminate();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                serverEventSender.pingAgent();
            } catch (RemoteException e) {
                publishEventOnAgentDisconnected();
                terminate();
            }

            try {
                Thread.sleep(PING_INTERVAL);
            } catch (InterruptedException e) {
                String message = String.format("Pinging agent with id %s was interrupted.", agentId);
                LOGGER.warn(message);
            }
        }
    }

    /**
     * Terminates periodically sending ping requests to the agent.
     */
    public synchronized void terminate() {
        isRunning = false;
    }

    private void publishEventOnAgentDisconnected() {
        AgentDisconnectedEvent agentDisconnectedEvent = new AgentDisconnectedEvent(agentManager);
        eventService.publish(agentDisconnectedEvent);
    }

}
