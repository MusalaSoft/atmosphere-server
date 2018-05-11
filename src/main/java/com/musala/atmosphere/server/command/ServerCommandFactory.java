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

package com.musala.atmosphere.server.command;

import com.musala.atmosphere.server.Server;

/**
 * A {@link ServerCommand ServerCommand} factory. Builds a {@link ServerCommand ServerCommand} For a given
 * {@link ServerConsoleCommands ServerConsoleCommands} enumeration value.
 *
 * @author vladimir.vladimirov
 *
 */
public class ServerCommandFactory {
    private final Server server;

    public ServerCommandFactory(Server server) {
        this.server = server;
    }

    /**
     *
     * @param consoleCommand
     *        - a {@link ServerConsoleCommands consoleCommand} object
     * @return {@link ServerCommand ServerCommand} instance associated with the passed consoleCommand.
     */
    public ServerCommand getCommandInstance(ServerConsoleCommands consoleCommand) {
        ServerCommand resultCommand = null;
        switch (consoleCommand) {
            case SERVER_RUN: {
                resultCommand = new RunCommand(server);
                break;
            }
            case SERVER_HELP: {
                resultCommand = new HelpCommand(server);
                break;
            }
            case SERVER_EXIT: {
                resultCommand = new ExitCommand(server);
                break;
            }
        }

        return resultCommand;
    }
}
