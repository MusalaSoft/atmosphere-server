package com.musala.atmosphere.server.state;

import java.io.IOException;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.Server;

/**
 * Common class for all Server states.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class ServerState {
    protected ConsoleControl serverConsole;

    protected Server server;

    public ServerState(Server server, ConsoleControl serverConsole) {
        this.server = server;
        this.serverConsole = serverConsole;
    }

    /**
     * Prints a string to the Server's console output.
     * 
     * @param message
     *        - the message to be printed.
     */
    public void writeToConsole(String message) {
        serverConsole.write(message);
    }

    /**
     * Prints a line to the server's console output.
     * 
     * @param message
     *        - the message to be printed.
     */
    public void writeLineToConsole(String message) {
        serverConsole.writeLine(message);
    }

    /**
     * Reads one line from the server's console.
     * 
     * @return - the first line in the console buffer as a String.
     * @throws IOException
     *         - if a console reading error occurs.
     */
    public String readCommandFromConsole() throws IOException {
        String command = serverConsole.readCommand();
        return command;
    }

    // These methods are different for each server state.
    public abstract void run();

    public abstract void stop();

}
