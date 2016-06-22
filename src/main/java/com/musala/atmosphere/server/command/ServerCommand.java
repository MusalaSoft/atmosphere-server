package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Common class for console commands, executed by the server.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class ServerCommand {
    private static final String ILLEGAL_ARGUMENTS_MESSAGE = "Illegal command arguments. "
            + "Use 'help' for more information.";

    protected Server server;

    public ServerCommand(Server server) {
        this.server = server;
    }

    public void execute(String[] params) {
        if (!verifyParams(params)) {
            server.writeLineToConsole(ILLEGAL_ARGUMENTS_MESSAGE);
        } else {
            executeCommand(params);
        }
    }

    protected abstract boolean verifyParams(String[] params);

    protected abstract void executeCommand(String[] params);
}
