package com.musala.atmosphere.server.util;

import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class ServerPropertiesLoaderTest {

    @Test
    public void testPropertiesLoader() {
        int poolManagerPort = 0;
        poolManagerPort = ServerPropertiesLoader.getWebSocketPort();
        assertNotSame("Returns property.", 0, poolManagerPort);
    }
}
