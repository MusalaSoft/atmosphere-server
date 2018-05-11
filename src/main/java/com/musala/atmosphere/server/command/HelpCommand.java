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

import java.util.List;

import com.musala.atmosphere.server.Server;

/**
 * This class executes the "help" command. It prints on the standart output information about all available commands on
 * the Server. The command requires no parameters to be passed, otherwise it will not be executed.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class HelpCommand extends NoParamsServerCommand {
    public HelpCommand(Server server) {
        super(server);
    }

    /**
     * Prints information about every single command on the Server that can be executed.
     */
    @Override
    protected void executeCommand(String[] params) {
        List<String> listOfCommands = ServerConsoleCommands.getListOfCommands();
        if (listOfCommands != null) {
            for (String serverConsoleCommand : listOfCommands) {
                server.writeLineToConsole(serverConsoleCommand);
            }
        }
    }
}
