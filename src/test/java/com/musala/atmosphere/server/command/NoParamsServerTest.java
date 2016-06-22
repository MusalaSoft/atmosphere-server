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
