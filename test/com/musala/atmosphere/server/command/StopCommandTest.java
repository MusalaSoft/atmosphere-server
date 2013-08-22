package com.musala.atmosphere.server.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.server.Server;

public class StopCommandTest
{
	private Server innerServerMock;

	private ServerCommand stopCommand;

	private String[] emptyArray;

	@Before
	public void setUp()
	{
		innerServerMock = mock(Server.class);
		stopCommand = new StopCommand(innerServerMock);
		emptyArray = new String[] {};
	}

	@Test
	public void testExecuteCommand()
	{
		stopCommand.execute(emptyArray);

		verify(innerServerMock, times(1)).stop();
	}
}
