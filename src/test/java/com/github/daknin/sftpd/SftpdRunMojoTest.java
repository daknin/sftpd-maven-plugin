package com.github.daknin.sftpd;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.auth.UserAuthPasswordFactory;
import org.apache.sshd.server.auth.UserAuthPublicKeyFactory;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 18/06/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class SftpdRunMojoTest {
    private SftpdRunMojo sftpdRunMojo;
    @Mock
    private SshServer sshServer;
    @Mock
    private MavenProject mavenProject;
    private Properties mavenProperties;
    @Mock
    private ServerBuilder serverBuilder;
    @Mock
    private SecurityUtilsProvider securityUtilsProvider;
    @Mock
    private AbstractGeneratorHostKeyProvider hostKeyProvider;
    @Mock
    private File serverRoot;
    @Mock
    private File serverKey;

    @Captor
    private ArgumentCaptor<List<NamedFactory<UserAuth>>> authenticationMethodCaptor;

    @Captor
    private ArgumentCaptor<List<NamedFactory<Command>>> commandSubsystemCaptor;


    @Before
    public void setUp() throws Exception {
        mavenProperties = new Properties();
        when(mavenProject.getProperties()).thenReturn(mavenProperties);
        when(serverBuilder.build()).thenReturn(sshServer);
        when(securityUtilsProvider.createGeneratorHostKeyProvider(any(Path.class))).thenReturn(hostKeyProvider);
        sftpdRunMojo = new SftpdRunMojo(serverBuilder, securityUtilsProvider);
        sftpdRunMojo.setMavenProject(mavenProject);
        sftpdRunMojo.setServerRoot(serverRoot);
        sftpdRunMojo.setServerKey(serverKey);
        sftpdRunMojo.setUsername("username");
        sftpdRunMojo.setPort(2121);
    }

    @Test
    public void doesNothingIfSkipActivated() throws Exception {
        sftpdRunMojo.setSkip(true);

        sftpdRunMojo.execute();

        verify(sshServer, times(0)).start();
    }

    @Test
    public void whenKeyIsSuppliedShouldCreateServerWithKeyAuthenticationAndAddItToProjectProperties() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.setAuthorisedKeysFile(new File(""));
        sftpdRunMojo.execute();

        assertTrue(mavenProperties.containsKey(SftpdConstants.SFTPSERVER_KEY));

        verify(sshServer, times(1)).setUserAuthFactories(authenticationMethodCaptor.capture());
        assertTrue(authenticationMethodCaptor.getValue().size() == 1);
        assertTrue(authenticationMethodCaptor.getValue().get(0) instanceof UserAuthPublicKeyFactory);
    }

    @Test
    public void whenPasswordSuppliedShouldCreateServerWithPasswordAuthenticationAndAddItToProjectProperties() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.setPassword("password");
        sftpdRunMojo.execute();

        assertTrue(mavenProperties.containsKey(SftpdConstants.SFTPSERVER_KEY));

        verify(sshServer, times(1)).setUserAuthFactories(authenticationMethodCaptor.capture());
        assertTrue(authenticationMethodCaptor.getValue().size() == 1);
        assertTrue(authenticationMethodCaptor.getValue().get(0) instanceof UserAuthPasswordFactory);
    }

    @Test
    public void whenKeyAndPasswordNotSuppliedShouldCreateServerWithoutAuthenticationAndAddItToProjectProperties() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.execute();

        assertTrue(mavenProperties.containsKey(SftpdConstants.SFTPSERVER_KEY));

        verify(sshServer, times(1)).setUserAuthFactories(authenticationMethodCaptor.capture());
        assertTrue(authenticationMethodCaptor.getValue().size() == 1);
        assertTrue(authenticationMethodCaptor.getValue().get(0) instanceof UserAuthNoneFactory);
    }

    @Test
    public void shouldSetPortOnServer() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.execute();

        verify(sshServer, times(1)).setPort(2121);
    }

    @Test
    public void shouldSetupServerForScp() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.execute();

        verify(sshServer, times(1)).setCommandFactory(isA(ScpCommandFactory.class));
    }

    @Test
    public void shouldSetupServerForSftp() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.execute();

        verify(sshServer, times(1)).setSubsystemFactories(commandSubsystemCaptor.capture());
        assertTrue(commandSubsystemCaptor.getValue().size() == 1);
        assertTrue(commandSubsystemCaptor.getValue().get(0) instanceof SftpSubsystemFactory);
    }

    @Test
    public void shouldConfigureServerWithHostKey() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        sftpdRunMojo.execute();

        verify(sshServer, times(1)).setKeyPairProvider(hostKeyProvider);
    }

    @Test
    public void shouldCreateServerRootIfItDoesntExist() throws Exception {
        when(serverRoot.exists()).thenReturn(false);
        when(serverRoot.mkdir()).thenReturn(true);

        sftpdRunMojo.execute();

        verify(serverRoot, times(1)).mkdir();
    }

    @Test(expected = MojoFailureException.class)
    public void shouldThrowMojoExceptionIfCantFindOrCreateServerRoot() throws Exception {
        when(serverRoot.exists()).thenReturn(false);
        when(serverRoot.mkdir()).thenReturn(false);

        sftpdRunMojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void shouldThrowMojoExceptionIfCantStartServer() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        doThrow(new IOException()).when(sshServer).start();

        sftpdRunMojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void shouldThrowMojoExceptionIfCantAddServerToMavenProperties() throws Exception {
        when(serverRoot.exists()).thenReturn(true);
        when(mavenProject.getProperties()).thenReturn(null);
        sftpdRunMojo.setMavenProject(null);

        sftpdRunMojo.execute();
    }
}