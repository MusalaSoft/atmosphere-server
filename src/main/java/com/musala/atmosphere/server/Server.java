package com.musala.atmosphere.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import javax.websocket.DeploymentException;

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
import com.musala.atmosphere.server.eventservice.event.device.DeviceEvent;
import com.musala.atmosphere.server.monitor.AgentMonitor;
import com.musala.atmosphere.server.pool.ClientRequestMonitor;
import com.musala.atmosphere.server.registrymanager.RemoteObjectRegistryManager;
import com.musala.atmosphere.server.state.ServerState;
import com.musala.atmosphere.server.state.StoppedServer;
import com.musala.atmosphere.server.util.ServerPropertiesLoader;
import com.musala.atmosphere.server.websocket.ClientServerWebSocketCommunicator;
import com.musala.atmosphere.server.websocket.ClientServerWebSocketEndpoint;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getCanonicalName());

    private static final int AGENT_CONNECTION_CYCLE_WAIT = 300;

    private ServerManager serverManager;

    private org.glassfish.tyrus.server.Server server;

    private ConsoleControl serverConsole;

    private ServerCommandFactory commandFactory;

    private ServerState currentServerState;

    private ServerEventService eventService;

    private AgentMonitor agentMonitor;

    private RemoteObjectRegistryManager registryManager;

    private IDataSourceManager dataSourceManager;

    private IDataSourceProvider dataSourceProvider;

    private int serverRmiPort;

    private boolean isConnected;

    /**
     * Creates a Server component bound on specified in the properties file port.
     *
     * @throws RemoteException
     *         - thrown when an error during the execution of a remote method call.
     */
    public Server() throws RemoteException {
        this(ServerPropertiesLoader.getPoolManagerRmiPort());
    }

    /**
     * Creates a Server component bound on given port.
     *
     * @param serverPort
     *        - port on which the Pool Manager of the Server will be published in RMI.
     * @throws RemoteException
     *         - thrown when an error during the execution of a remote method call.
     */
    public Server(int serverPort) throws RemoteException {
        serverRmiPort = serverPort;
        serverManager = new ServerManager(serverRmiPort);
        ClientServerWebSocketCommunicator.getInstance().setServerManager(serverManager);

        // TODO WebSockets: Add support for hostName and port configuration
        server = new org.glassfish.tyrus.server.Server("localhost", 80, null, null, ClientServerWebSocketEndpoint.class);
        try {
            server.start();
        } catch (DeploymentException e) {
            LOGGER.error("Could not start WebSocket server.");
        }

        setState(new StoppedServer(this));

        serverConsole = new ConsoleControl();
        commandFactory = new ServerCommandFactory(this);

        eventService = new ServerEventService();
        agentMonitor = new AgentMonitor();

        registryManager = new RemoteObjectRegistryManager(serverManager.getRegistry());

        // Add subscribers to the event service for agent events.
        eventService.subscribe(AgentEvent.class, serverManager);
        eventService.subscribe(AgentEvent.class, agentMonitor);

        // Add subscribers to the event service for device events.
        eventService.subscribe(DeviceEvent.class, registryManager);

        eventService.subscribe(DevicePoolDaoCreatedEvent.class, serverManager);

        dataSourceManager = new DataSourceManager(new DataSourceCallback());
        dataSourceProvider = new DataSourceProvider();

        eventService.subscribe(DataSourceInitializedEvent.class, dataSourceProvider);

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
    }

    /**
     * Stops the Server if it's running.
     */
    public void stop() {
        currentServerState.stop();
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
        server.stop();
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

    public static void main(String[] args) throws NotBoundException, IOException, InterruptedException {
        // Check if an argument which specifies a port for the Server was passed.
        int portToCreateServerOn = 0;
        try {
            if (args.length == 1) {
                String passedRmiPort = args[0];
                portToCreateServerOn = Integer.parseInt(passedRmiPort);
            } else {
                portToCreateServerOn = ServerPropertiesLoader.getPoolManagerRmiPort();
            }
        } catch (NumberFormatException e) {
            String errorMessage = "Parsing passed port resulted in an exception.";
            LOGGER.fatal(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        Server localServer = new Server(portToCreateServerOn);
        localServer.run();
        do {
            String passedShellCommand = localServer.readCommandFromConsole();
            localServer.parseAndExecuteShellCommand(passedShellCommand);
        } while (!localServer.isClosed());
    }
}