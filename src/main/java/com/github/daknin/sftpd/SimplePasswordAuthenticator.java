package com.github.daknin.sftpd;

import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 *
 */
public class SimplePasswordAuthenticator extends AbstractLoggingBean implements PasswordAuthenticator {
    private final String username;
    private final String password;

    public SimplePasswordAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public final boolean authenticate(String username, String password, ServerSession session) {
        boolean accepted = this.username.equals(username) && this.password.equals(password);
        if (accepted) {
            handleAcceptance(username, password, session);
        } else {
            handleRejection(username, password, session);
        }

        return accepted;
    }

    protected void handleAcceptance(String username, String password, ServerSession session) {
        log.info("authenticate({}[{}]: accepted", username, session);
    }

    protected void handleRejection(String username, String password, ServerSession session) {
        log.info("authenticate({}[{}]: rejected", username, session);

    }
}
