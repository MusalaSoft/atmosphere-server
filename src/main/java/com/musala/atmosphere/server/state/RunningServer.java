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

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.server.Server;

/**
 * This class represents the running state of the Server. It creates a new inner thread which prevents the Server from
 * closing until explicitly stated so.
 *
 * @author vladimir.vladimirov
 *
 */
public class RunningServer extends ServerState {
    private static final Logger LOGGER = Logger.getLogger(RunningServer.class.getCanonicalName());

    private static final String SERVER_ALREADY_RUNNING_ERROR = "Command invalid: Server already running.";

    private Thread serverThread;

    private boolean isRunning;

    public RunningServer(Server server, ConsoleControl serverConsole) {
        super(server, serverConsole);
        InnerRunThread innerThread = new InnerRunThread();
        serverThread = new Thread(innerThread, "ServerRunningWaitThread");
        serverThread.start();

        isRunning = true;
    }

    @Override
    public void run() {
        LOGGER.warn(SERVER_ALREADY_RUNNING_ERROR);
    }

    @Override
    public void stop() {
        server.setState(new StoppedServer(server, serverConsole));
        isRunning = false;
    }

    private class InnerRunThread implements Runnable {
        @Override
        public void run() {
            LOGGER.info("Running Server...");
            String serverStartedMessage = "The Server has started successfully.";
            writeLineToConsole(serverStartedMessage);
            while (isRunning) {
                try {
                    // we must make sure the server thread is not stopped until we say so
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.warn("Server wait thread was interrupted.", e);
                    server.exit();
                    break;
                }
            }
        }
    }
}
