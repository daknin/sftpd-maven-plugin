package com.github.daknin.sftpd;

import org.apache.maven.settings.Server;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created on 18/06/2016.
 */
public class SimplePasswordAuthenticatorTest {
    private SimplePasswordAuthenticator simplePasswordAuthenticator;
    private static final String USERNAME = "TheUsername";

    private static final String PASSWORD = "ThePassword";

    private ServerSession serverSession;

    @Before
    public void setUp() throws Exception {
        simplePasswordAuthenticator = new SimplePasswordAuthenticator(USERNAME, PASSWORD);
    }

    @Test
    public void authenticationSucceedsWhenCorrectUsernameAndPasswordSupplied() throws Exception {
        assertTrue(simplePasswordAuthenticator.authenticate(USERNAME, PASSWORD, serverSession));
    }

    @Test
    public void authenticationFailsWhenCorrectUsernameAndIncorrectPasswordSupplied() throws Exception {
        assertFalse(simplePasswordAuthenticator.authenticate(USERNAME, "AnotherPassword", serverSession));
    }

    @Test
    public void authenticationFailsWhenIncorrectCorrectUsernameAndCorrectPasswordSupplied() throws Exception {
        assertFalse(simplePasswordAuthenticator.authenticate("AnotherUsername", PASSWORD, serverSession));
    }

    @Test
    public void authenticationFailsWhenIncorrectUsernameAndIncorrectPasswordSupplied() throws Exception {
        assertFalse(simplePasswordAuthenticator.authenticate("AnotherUsername", "AnotherPassword", serverSession));
    }
}