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

import java.util.ArrayList;
import java.util.List;

/**
 * Enumerates all possible first arguments of shell commands available for Server.
 *
 * @author vladimir.vladimirov
 *
 */
public enum ServerConsoleCommands {
    SERVER_RUN("run", "run", "Runs the Server."),
    SERVER_HELP("help", "help", "Prints all available commands."),
    SERVER_EXIT("exit", "exit", "Stops and closes the currently running Server component.");

    private String command;

    private String syntax;

    private String description;

    private ServerConsoleCommands(String command, String commmandSyntax, String commandDescription) {
        this.command = command;
        this.syntax = commmandSyntax;
        this.description = commandDescription;
    }

    /**
     * @return the current command string.
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return current command description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return syntax of the current command as a string.
     */
    public String getSyntax() {
        return syntax;
    }

    /**
     * Gets list with all the commands that can be passed to the console to manage the Server. For every available
     * command there is a String which is in the following format: "<b>Command format</b> - <b>Command Description</b>"
     * where <b>"Command format"</b> is pattern how to write the given command and what arguments it can be passed,
     * while the <b>"Command description"</b> says what the command does.
     *
     * @return - a list with all the commands that can be passed to the console to manage the Server.
     */
    public static List<String> getListOfCommands() {
        List<String> allCommandsFullInformation = new ArrayList<>();
        for (ServerConsoleCommands currentCommand : ServerConsoleCommands.values()) {
            String description = currentCommand.getDescription();
            String syntax = currentCommand.getSyntax();
            String currentCommandInfo = String.format("%-25s %s", syntax, description);
            allCommandsFullInformation.add(currentCommandInfo);
        }
        return allCommandsFullInformation;
    }

    /**
     * Searches for command by given command name. Returns null if no corresponding command is not found.
     *
     * @param commandName
     *        - the name of the command
     * @return a {@link ServerConsoleCommands ServerConsoleCommands} instance.
     */
    public static ServerConsoleCommands findCommand(String commandName) {
        ServerConsoleCommands resultCommand = null;

        for (ServerConsoleCommands possibleCommand : ServerConsoleCommands.values()) {
            String possibleCommandString = possibleCommand.getCommand();
            if (possibleCommandString.equalsIgnoreCase(commandName)) {
                resultCommand = possibleCommand;
                break;
            }
        }

        return resultCommand;
    }
}
