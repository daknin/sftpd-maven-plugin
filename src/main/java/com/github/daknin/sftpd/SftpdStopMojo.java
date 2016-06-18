package com.github.daknin.sftpd;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.sshd.server.SshServer;

import java.util.Properties;


@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class SftpdStopMojo extends AbstractSftpdMojo {

    public void execute() throws MojoFailureException {
        if (isSkip()) {
            return;
        }
        getLog().info("Stopping SFTP server...");
        Properties properties = null;
        if (mavenProject != null) {
            properties = mavenProject.getProperties();
        } else {
            throw new MojoFailureException("Can't access maven project to stop SFTP server (null)");
        }

        if (properties != null) {
            SshServer sshd;
            try {
                sshd = (SshServer) properties.get(SftpdConstants.SFTPSERVER_KEY);
            } catch (ClassCastException e) {
                throw new MojoFailureException("Context doesn't contain a valid SFTP server instance", e);
            }
            if (sshd == null) {
                throw new MojoFailureException("Context doesn't contain any SFTP server instance");
            }
            if (!sshd.isClosed()) {
                try {
                    sshd.close();
                } catch (Exception e) {
                    throw new MojoFailureException("Failed to stop SFTP server", e);
                }
                getLog().info("SFTP server stopped.");
            } else {
                getLog().info("SFTP server was stopped already");
            }
        } else {
            throw new MojoFailureException("Maven project has null properties", new NullPointerException());
        }

    }
}
