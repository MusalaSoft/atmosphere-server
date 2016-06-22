package com.musala.atmosphere.server.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.server.Server;

public class RunCommandTest {
    private Server innerServerMock;

    private ServerCommand runCommand;

    private String[] emptyArray;

    @Before
    public void setUp() {
        innerServerMock = mock(Server.class);
        runCommand = new RunCommand(innerServerMock);
        emptyArray = new String[] {};
    }

    @Test
    public void testExecuteCommand() {
        runCommand.execute(emptyArray);

        verify(innerServerMock, times(1)).run();
    }
}
