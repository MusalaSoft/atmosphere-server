package com.musala.atmosphere.server.state;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.Server;

/**
 * Class that represents the stopped Server state.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class StoppedServer extends ServerState {
    private static final String SERVER_NOT_RUNNING_MESSAGE = "Invalid command: server not running.";

    public StoppedServer(Server server, ConsoleControl serverConsole) {
        super(server, serverConsole);
    }

    public StoppedServer(Server server) {
        this(server, new ConsoleControl());
    }

    @Override
    public void run() {
        server.setState(new RunningServer(server, serverConsole));
    }

    @Override
    public void stop() {
        server.writeLineToConsole(SERVER_NOT_RUNNING_MESSAGE);
    }

}
