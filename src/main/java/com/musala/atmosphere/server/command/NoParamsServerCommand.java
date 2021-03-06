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
 * Common class for all server commands that require no parameters.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class NoParamsServerCommand extends ServerCommand {
    public NoParamsServerCommand(Server server) {
        super(server);
    }

    @Override
    protected boolean verifyParams(String[] params) {
        if (params != null && params.length != 0) {
            return false;
        }
        return true;
    }
}
