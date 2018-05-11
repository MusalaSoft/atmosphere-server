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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.server.Server;

public class ServerCommandTest {
    private ServerCommand mockedServerCommand;

    private String[] randomParams;

    @Before
    public void setUp() {
        Server innerServerMock = mock(Server.class);
        mockedServerCommand = spy(new ServerCommand(innerServerMock) {
            @Override
            protected boolean verifyParams(String[] params) {
                return false;
            }

            @Override
            protected void executeCommand(String[] params) {
            }
        });
        randomParams = new String[] {"1", "2"};
    }

    @Test
    public void testExecuteCommandWithValidParams() {
        when(mockedServerCommand.verifyParams(any(String[].class))).thenReturn(true);
        mockedServerCommand.execute(randomParams);

        verify(mockedServerCommand, times(1)).executeCommand(eq(randomParams));
    }

    @Test
    public void testExecuteCommandWithInvalidParams() {
        when(mockedServerCommand.verifyParams(any(String[].class))).thenReturn(false);
        mockedServerCommand.execute(randomParams);

        verify(mockedServerCommand, never()).executeCommand(any(String[].class));
    }
}
