package com.github.daknin.sftpd;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.auth.UserAuthPasswordFactory;
import org.apache.sshd.server.auth.UserAuthPublicKeyFactory;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Mojo(name = "run", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class SftpdRunMojo extends AbstractSftpdMojo {
    private ServerBuilder serverBuilder;
    private SecurityUtilsProvider securityUtilsProvider;

    public SftpdRunMojo() {
        serverBuilder = ServerBuilder.builder();
        securityUtilsProvider = new MenaSecurityUtilsProvider();
    }

    SftpdRunMojo(ServerBuilder serverBuilder, SecurityUtilsProvider securityUtilsProvider) {
        this.serverBuilder = serverBuilder;
        this.securityUtilsProvider = securityUtilsProvider;
    }

    public void execute() throws MojoFailureException {
        if (isSkip()) {
            return;
        }
        boolean serverRootExists = getServerRoot().exists();
        if (!serverRootExists) {
            serverRootExists = getServerRoot().mkdir();
        }
        SshServer sshd;
        if (!serverRootExists) {
            throw new MojoFailureException("Failed to create SFTP root " + getServerRoot().getPath());
        }

        sshd = createServer();
        try {
            sshd.start();
            getLog().info("Started SFTP Server on port " + port);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to start SFTP server", e);
        }
        if (getMavenProject() != null) {
            Properties properties = getMavenProject().getProperties();
            properties.put(SftpdConstants.SFTPSERVER_KEY, sshd);
        } else {
            throw new MojoFailureException("Can't add sftpserver instance as maven project is null");
        }
    }

    private SshServer createServer() throws MojoFailureException {
        getLog().info("About to start SFTP server...");
        SshServer sshd = serverBuilder.build();
        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        if (authorisedKeysFile != null) {
            sshd.setPublickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator(getUsername(), getAuthorisedKeysFile(), false));
            userAuthFactories.add(new UserAuthPublicKeyFactory());
            getLog().info("Authentication configured using username: " + getUsername() + " authorized_keys: " + getAuthorisedKeysFile());
        }
        if (password != null) {
            sshd.setPasswordAuthenticator(new SimplePasswordAuthenticator(getUsername(), getPassword()));
            userAuthFactories.add(new UserAuthPasswordFactory());
            getLog().info("Authentication configured using username: " + getUsername() + " password: " + getPassword());
        }
        if (authorisedKeysFile == null && password == null) {
            userAuthFactories.add(new UserAuthNoneFactory());
        }
        sshd.setUserAuthFactories(userAuthFactories);
        sshd.setPort(getPort());

        AbstractGeneratorHostKeyProvider hostKeyProvider = securityUtilsProvider.createGeneratorHostKeyProvider(getServerKey().toPath());
        hostKeyProvider.setAlgorithm("RSA");
        sshd.setKeyPairProvider(hostKeyProvider);

        sshd.setFileSystemFactory(new VirtualFileSystemFactory(getServerRoot().getAbsolutePath()));

        sshd.setCommandFactory(new ScpCommandFactory());

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystemFactory());
        sshd.setSubsystemFactories(namedFactoryList);
        return sshd;
    }
}
