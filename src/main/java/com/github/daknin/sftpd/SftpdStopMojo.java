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
        getLog().info("Stopping FTP server...");
        Properties properties = null;
        if (mavenProject != null) {
            properties = mavenProject.getProperties();
        } else {
            throw new MojoFailureException("Can't access maven project to stop FTP server (null)");
        }

        if (properties != null) {
            SshServer sshd;
            try {
                sshd = (SshServer) properties.get(SftpdConstants.FTPSERVER_KEY);
            } catch (ClassCastException e) {
                throw new MojoFailureException("Context doesn't contain a valid ftp server instance", e);
            }
            if (sshd == null) {
                throw new MojoFailureException("Context doesn't contain any ftp server instance");
            }
            if (!sshd.isClosed()) {
                try {
                    sshd.close();
                } catch (Exception e) {
                    throw new MojoFailureException("Failed to stop Sftpd", e);
                }
                getLog().info("FTP server stopped.");
            } else {
                getLog().info("FTP server was stopped already");
            }
        } else {
            throw new MojoFailureException("Maven project has null properties", new NullPointerException());
        }

    }
}
