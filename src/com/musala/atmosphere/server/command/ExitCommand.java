package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * Class that specifies the "stop" command behavior.
 * 
 * @author vladimir.vladimirov
 */
public class ExitCommand extends NoParamsServerCommand {
    public ExitCommand(Server server) {
        super(server);
    }

    @Override
    protected void executeCommand(String[] params) {
        server.exit();
    }
}
