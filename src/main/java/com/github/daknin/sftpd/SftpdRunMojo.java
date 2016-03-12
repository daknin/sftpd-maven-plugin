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

    private SshServer sshd;

    public void execute() throws MojoFailureException {
        getLog().info("Server root is " + serverRoot.getPath());
        boolean serverRootExists = serverRoot.exists();
        if (!serverRootExists) {
            serverRootExists = serverRoot.mkdir();
        }
        if (serverRootExists) {
            initServer();
            try {
                sshd.start();
            } catch (IOException e) {
                throw new MojoFailureException("Failed to start SFTP server", e);
            }
        } else {
            throw new MojoFailureException("Failed to create FTP root " + serverRoot.getPath());
        }
        if (mavenProject != null) {
            Properties properties = mavenProject.getProperties();
            properties.put(SftpdConstants.FTPSERVER_KEY, sshd);
        } else {
            throw new MojoFailureException("Can't add ftpserver instance as maven project is null");
        }
    }

    private void initServer() throws MojoFailureException {
        getLog().info("About to start FTP server...");
        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        if (authorisedKeysFile != null) {
            sshd = ServerBuilder.builder()
                    .publickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator("theUser", authorisedKeysFile, false))
                    .build();
            userAuthFactories.add(new UserAuthPublicKeyFactory());
        } else {
            sshd = ServerBuilder.builder().build();
        }
        if (password != null) {
            sshd.setPasswordAuthenticator(new SimplePasswordAuthenticator(username, password));
            userAuthFactories.add(new UserAuthPasswordFactory());
        }
        if (authorisedKeysFile == null && password == null) {
            userAuthFactories.add(new UserAuthNoneFactory());
        }
        
        sshd.setPort(port);

        AbstractGeneratorHostKeyProvider hostKeyProvider = SecurityUtils.createGeneratorHostKeyProvider(serverKey.toPath());
        hostKeyProvider.setAlgorithm("RSA");
        sshd.setKeyPairProvider(hostKeyProvider);

        sshd.setFileSystemFactory(new VirtualFileSystemFactory(serverRoot.getAbsolutePath()));

        userAuthFactories.add(new UserAuthPublicKeyFactory());
        sshd.setUserAuthFactories(userAuthFactories);

        sshd.setCommandFactory(new ScpCommandFactory());

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystemFactory());
        sshd.setSubsystemFactories(namedFactoryList);
    }
}
