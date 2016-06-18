package com.github.daknin.sftpd;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.sshd.server.SshServer;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 18/06/2016.
 */
public class SftpdStopMojoTest {
    private SftpdStopMojo sftpdStopMojo;
    private SshServer sshServer;
    private MavenProject mavenProject;
    private Properties mavenProperties;

    @Before
    public void setUp() throws Exception {
        mavenProperties = new Properties();
        sshServer = mock(SshServer.class);
        mavenProject = mock(MavenProject.class);
        when(mavenProject.getProperties()).thenReturn(mavenProperties);
        sftpdStopMojo = new SftpdStopMojo();
        sftpdStopMojo.setMavenProject(mavenProject);
    }

    @Test
    public void stopsServerIfReferenceCanBeRetrievedFromProject() throws Exception {
        mavenProperties.put(SftpdConstants.SFTPSERVER_KEY, sshServer);

        sftpdStopMojo.execute();

        verify(sshServer, times(1)).stop();
    }

    @Test
    public void doesntStopServerIfItIsntRunning() throws Exception {
        mavenProperties.put(SftpdConstants.SFTPSERVER_KEY, sshServer);
        when(sshServer.isClosed()).thenReturn(true);

        sftpdStopMojo.execute();

        verify(sshServer, times(0)).stop();
    }

    @Test
    public void doesNothingIfSkipActivated() throws Exception {
        sftpdStopMojo.setSkip(true);

        sftpdStopMojo.execute();

        verify(sshServer, times(0)).stop();
    }

    @Test(expected = MojoFailureException.class)
    public void throwsMojoExceptionIfUnableToAccessProject() throws Exception {
        sftpdStopMojo.setMavenProject(null);

        sftpdStopMojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void throwsMojoExceptionIfUnableToAccessProperties() throws Exception {
        mavenProject = mock(MavenProject.class);
        when(mavenProject.getProperties()).thenReturn(null);
        sftpdStopMojo.setMavenProject(mavenProject);

        sftpdStopMojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void throwsMojoExceptionIfUnableToRetrieveServerReferenceFromProject() throws Exception {
        sftpdStopMojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void throwsMojoExceptionIfServerReferenceIsWrongType() throws Exception {
        mavenProperties.put(SftpdConstants.SFTPSERVER_KEY, "String");

        sftpdStopMojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void throwsMojoExceptionIfExceptionThrownWhenStoppingServer() throws Exception {
        doThrow(new NullPointerException()).when(sshServer).stop();
        mavenProperties.put(SftpdConstants.SFTPSERVER_KEY, sshServer);

        sftpdStopMojo.execute();
    }
}