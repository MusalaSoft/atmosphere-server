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
