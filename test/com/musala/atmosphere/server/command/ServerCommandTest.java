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

public class ServerCommandTest
{
	private ServerCommand mockedServerCommand;

	private String[] randomParams;

	@Before
	public void setUp()
	{
		Server innerServerMock = mock(Server.class);
		mockedServerCommand = spy(new ServerCommand(innerServerMock)
		{
			@Override
			protected boolean verifyParams(String[] params)
			{
				return false;
			}

			@Override
			protected void executeCommand(String[] params)
			{
			}
		});
		randomParams = new String[] {"1", "2"};
	}

	@Test
	public void testExecuteCommandWithValidParams()
	{
		when(mockedServerCommand.verifyParams(any(String[].class))).thenReturn(true);
		mockedServerCommand.execute(randomParams);

		verify(mockedServerCommand, times(1)).executeCommand(eq(randomParams));
	}

	@Test
	public void testExecuteCommandWithInvalidParams()
	{
		when(mockedServerCommand.verifyParams(any(String[].class))).thenReturn(false);
		mockedServerCommand.execute(randomParams);

		verify(mockedServerCommand, never()).executeCommand(any(String[].class));
	}
}
