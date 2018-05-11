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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.server.Server;

public class NoParamsServerTest {
    private ServerCommand noParamsCommand;

    @Before
    public void setUp() {
        Server innerServerMock = mock(Server.class);
        noParamsCommand = new NoParamsServerCommand(innerServerMock) {
            @Override
            protected void executeCommand(String[] params) {
            }
        };
    }

    @Test
    public void testVerifyParamsWithValidParams() {
        assertTrue(noParamsCommand.verifyParams(new String[] {}));
        assertTrue(noParamsCommand.verifyParams(null));
    }

    @Test
    public void testVerifyParamsWithInvalidParams() {
        assertFalse(noParamsCommand.verifyParams(new String[] {""}));
        assertFalse(noParamsCommand.verifyParams(new String[] {"1", "2"}));
    }
}
