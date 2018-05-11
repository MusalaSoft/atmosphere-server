// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.server;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.server.command.ServerCommand;
import com.musala.atmosphere.server.command.ServerCommandFactory;
import com.musala.atmosphere.server.command.ServerConsoleCommands;
import com.musala.atmosphere.server.data.IDataSourceManager;
import com.musala.atmosphere.server.data.db.flyway.DataSourceCallback;
import com.musala.atmosphere.server.data.db.flyway.DataSourceManager;
import com.musala.atmosphere.server.data.provider.IDataSourceProvider;
import com.musala.atmosphere.server.data.provider.ormlite.DataSourceProvider;
import com.musala.atmosphere.server.eventservice.ServerEventService;
import com.musala.atmosphere.server.eventservice.event.agent.AgentEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.DataSourceInitializedEvent;
import com.musala.atmosphere.server.eventservice.event.datasource.create.dao.DevicePoolDaoCreatedEvent;
import com.musala.atmosphere.server.eventservice.event.device.allocate.DeviceReleasedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DevicePublishedEvent;
import com.musala.atmosphere.server.eventservice.event.device.publish.DeviceUnpublishedEvent;
import com.musala.atmosphere.server.monitor.AgentMonitor;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;
import com.musala.atmosphere.server.state.ServerState;
import com.musala.atmosphere.server.state.StoppedServer;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;
import com.musala.atmosphere.server.websocket.ServerDispatcher;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getCanonicalName());

    private static final int AGENT_CONNECTION_CYCLE_WAIT = 300;

    private ServerManager serverManager;

    private ConsoleControl serverConsole;

    private ServerCommandFactory commandFactory;

    private ServerState currentServerState;

    private ServerEventService eventService;

    private AgentMonitor agentMonitor;

    private IDataSourceManager dataSourceManager;

    private IDataSourceProvider dataSourceProvider;

    private DeviceAllocationManager allocationManager;

    private boolean isConnected;

    private String serverIp;

    private int serverPort;

    private ServerDispatcher dispatcher = ServerDispatcher.getInstance();

    /**
     * Creates a Server component on specified in the properties file IP and port.
     *
     */
    public Server() {
        this(ServerPropertiesLoader.getServerIp(), ServerPropertiesLoader.getWebSocketPort());
    }

    /**
     * Creates a Server component bound on given IP and port.
     * 
     * @param serverIp
     *        - the IP of the Server
     * @param serverPort
     *        - the port the server is listening to
     */
    public Server(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        serverManager = new ServerManager();

        setState(new StoppedServer(this));

        serverConsole = new ConsoleControl();
        commandFactory = new ServerCommandFactory(this);

        eventService = new ServerEventService();
        agentMonitor = new AgentMonitor();

        // Add subscribers to the event service for agent events.
        eventService.subscribe(AgentEvent.class, serverManager);
        eventService.subscribe(AgentEvent.class, agentMonitor);

        eventService.subscribe(DeviceUnpublishedEvent.class, serverManager);
        eventService.subscribe(DevicePoolDaoCreatedEvent.class, serverManager);

        dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceProvider = new DataSourceProvider();

        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);

        allocationManager = new DeviceAllocationManager(dataSourceProvider.getDevicePoolDao());

        eventService.subscribe(DeviceReleasedEvent.class, allocationManager);
        eventService.subscribe(DevicePublishedEvent.class, allocationManager);

        dispatcher.setServerManager(serverManager);
        dispatcher.setAllocationManager(allocationManager);

        isConnected = false;
        LOGGER.info("Server instance created succesfully.");
    }

    /**
     * Sets the server state.
     *
     * @param newState
     *        - the state of the server
     */
    public void setState(ServerState newState) {
        currentServerState = newState;
    }

    /**
     * Starts the Server thread if it is not already running.
     */
    public void run() {
        dataSourceManager.initialize();
        currentServerState.run();
        dispatcher.startWebSocketServer(serverIp, serverPort);
    }

    /**
     * Stops the Server if it's running.
     */
    public void stop() {
        currentServerState.stop();
        agentMonitor.terminate();
        dispatcher.stopWebsocketServer();
    }

    /**
     * Releases all resources used by the server and marks it as closed. After that the Server is no longer available
     * and should be started again in order to be used.
     */
    public void exit() {
        stop();
        ClientRequestMonitor deviceMonitor = new ClientRequestMonitor();
        deviceMonitor.stop();

        // Remove subscribers from event service.
        eventService.unsubscribe(AgentEvent.class, null, agentMonitor);
        eventService.unsubscribe(AgentEvent.class, null, serverManager);
        eventService.unsubscribe(DataSourceInitializedEvent.class, null, dataSourceProvider);

        serverManager.close();
        isConnected = true;
    }

    /**
     * Prints a string to the Server's console output.
     *
     * @param message
     *        - the message to be printed.
     */
    public void writeToConsole(String message) {
        currentServerState.writeToConsole(message);
    }

    /**
     * Prints a line to the Server's console output.
     *
     * @param message
     *        - the message to be printed.
     */
    public void writeLineToConsole(String message) {
        currentServerState.writeLineToConsole(message);
    }

    /**
     * Executes a passed command from the console.
     *
     * @param passedShellCommand
     *        - the passed shell command.
     * @throws IOException
     */
    private void parseAndExecuteShellCommand(String passedShellCommand) throws IOException {
        if (passedShellCommand != null) {
            Pair<String, List<String>> parsedCommand = ConsoleControl.parseShellCommand(passedShellCommand);
            String command = parsedCommand.getKey();
            List<String> paramsAsList = parsedCommand.getValue();

            if (!command.isEmpty()) {
                String[] params = new String[paramsAsList.size()];
                paramsAsList.toArray(params);
                executeShellCommand(command, params);
            }
        } else {
            LOGGER.error("Error in console: trying to execute 'null' as a command.");
            throw new IllegalArgumentException("Command passed to server is 'null'");
        }
    }

    /**
     * Evaluates a passed command and calls the appropriate Server method.
     *
     * @param commandName
     *        - command for execution.
     * @param params
     *        - passed command arguments.
     */
    private void executeShellCommand(String commandName, String[] params) {
        ServerConsoleCommands command = ServerConsoleCommands.findCommand(commandName);

        if (command == null) {
            currentServerState.writeLineToConsole("Unknown command. Use 'help' to retrieve list of available commands.");
            return;
        }

        ServerCommand executableCommand = commandFactory.getCommandInstance(command);
        executableCommand.execute(params);
    }

    /**
     * Reads one line from the server's console.
     *
     * @return the first line in the console buffer as a String.
     * @throws IOException
     *         - when a console reading error occurs.
     */
    public String readCommandFromConsole() throws IOException {
        String command = serverConsole.readCommand();
        return command;
    }

    /**
     *
     * @return true if the server is closed, false otherwise.
     */
    private boolean isClosed() {
        return isConnected;
    }

    /**
     * Checks if given agent is connected to the server.
     *
     * @param agentId
     *        - id of agent we are interested in.
     * @return true, if agent with the passed id is connected to the server, and false otherwise.
     */
    public boolean isAgentWithIdConnected(String agentId) {
        List<String> connectedAgentIds = serverManager.getAllConnectedAgentIds();
        return connectedAgentIds.contains(agentId);
    }

    /**
     * @return - true, if any running agents are connected to the server, and false otherwise.
     */
    public boolean hasAnyConnectedAgents() {
        return !serverManager.getAllConnectedAgentIds().isEmpty();
    }

    /**
     * Waits for any agent to connect to the server.
     */
    public void waitForAgentConnection() {
        while (!hasAnyConnectedAgents()) {
            try {
                Thread.sleep(AGENT_CONNECTION_CYCLE_WAIT);
            } catch (InterruptedException e) {
                LOGGER.info("Waiting for connected agents was interrupted.");
            }
        }
    }

    /**
     * Waits for expected agent to connect to the server.
     *
     * @param agentId
     *        - id of agent
     */
    public void waitForGivenAgentToConnect(String agentId) {
        while (!isAgentWithIdConnected(agentId)) {
            try {
                Thread.sleep(AGENT_CONNECTION_CYCLE_WAIT);
            } catch (InterruptedException e) {
                // Wait was interrupted, no one cares. Nothing to do here.
                String exceptionMessage = String.format("Waiting for agent with id %s was interrupted.", agentId);
                LOGGER.info(exceptionMessage);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Check if an argument which specifies a port for the Server was passed.
        int portToCreateServerOn = 0;
        try {
            if (args.length == 1) {
                String passedWebSocketPort = args[0];
                portToCreateServerOn = Integer.parseInt(passedWebSocketPort);
            } else {
                portToCreateServerOn = ServerPropertiesLoader.getWebSocketPort();
            }
        } catch (NumberFormatException e) {
            String errorMessage = "Parsing passed port resulted in an exception.";
            LOGGER.fatal(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        Server localServer = new Server(ServerPropertiesLoader.getServerIp(), portToCreateServerOn);
        localServer.run();
        do {
            String passedShellCommand = localServer.readCommandFromConsole();
            localServer.parseAndExecuteShellCommand(passedShellCommand);
        } while (!localServer.isClosed());
    }
}
