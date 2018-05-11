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
