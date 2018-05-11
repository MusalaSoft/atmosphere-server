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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.server.Server;

public class ExitCommandTest {
    private Server innerServerMock;

    private ServerCommand stopCommand;

    private String[] emptyArray;

    @Before
    public void setUp() {
        innerServerMock = mock(Server.class);
        stopCommand = new ExitCommand(innerServerMock);
        emptyArray = new String[] {};
    }

    @Test
    public void testExecuteCommand() {
        stopCommand.execute(emptyArray);

        verify(innerServerMock, times(1)).exit();
    }
}
