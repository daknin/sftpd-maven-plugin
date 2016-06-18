package com.github.daknin.sftpd;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created on 30/01/2016.
 */
public class MojoTest {

    private SftpdRunMojo startMojo;
    private SftpdStopMojo stopMojo;
    private String userDir;
    private int port;
    private static final String REMOTE_FILENAME = "remotefile.txt";

    @Before
    public void setUp() throws Exception {
        userDir = System.getProperty("user.dir");
        port = 2223;
        Model model = new Model();
        MavenProject project = new MavenProject(model);

        startMojo = new SftpdRunMojo();
        startMojo.setMavenProject(project);
        startMojo.setPort(port);
        startMojo.setServerRoot(new File(userDir + "/target/sftpd"));
        startMojo.setServerKey(new File(userDir + "/target/sftpd/hostkey.pem"));

        stopMojo = new SftpdStopMojo();
        stopMojo.setMavenProject(project);
    }

    @Test
    public void canTransferFileUsingPasswordAuthentication() throws Exception {
        String username = "theUser";
        String password = "password";
        startMojo.setUsername(username);
        startMojo.setPassword(password);
        startMojo.execute();

        String localFilename = "target/localfileKey.txt";
        sftpFileUsingPassword(username, password, REMOTE_FILENAME, localFilename);

        stopMojo.execute();

        assertTrue(new File(localFilename).exists());
    }

    @Test
    public void canTransferFileUsingKeyAuthentication() throws Exception {
        String userDir = System.getProperty("user.dir");
        String username = "theUser";
        startMojo.setUsername(username);
        String privateKeyFilename = userDir + "/target/id_rsa";
        String authorizedKeysFilename = userDir + "/target/sftpd/authorized_keys";
        String keyPassword = "password";
        generateRsaKeyPair(privateKeyFilename, authorizedKeysFilename, keyPassword);
        startMojo.setAuthorisedKeysFile(new File(authorizedKeysFilename));
        startMojo.execute();

        String localFilename = "target/localfilePassword.txt";
        sftpFileUsingPublicKey(username, privateKeyFilename, keyPassword, REMOTE_FILENAME, localFilename);

        stopMojo.execute();

        assertTrue(new File(localFilename).exists());
    }

    @Test
    public void ifKeyFileAndPasswordAreNotSetCanTransferFileUsingAnyUserAndPassword() throws Exception {
        startMojo.setUsername("theUser");

        startMojo.execute();

        String localFilename = "target/localfileNoAuth.txt";
        sftpFileUsingPassword("", "", REMOTE_FILENAME, localFilename);

        stopMojo.execute();

        assertTrue(new File(localFilename).exists());
    }

    private void sftpFileUsingPassword(String username, String password,
                                       String remoteFilename, String localFilename) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = null;
        session = jsch.getSession(username, "127.0.0.1", port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get(remoteFilename, localFilename);
        sftpChannel.exit();
        session.disconnect();
    }

    private void sftpFileUsingPublicKey(String username, String privateKeyFilename, String password,
                                        String remoteFilename, String localFilename) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = null;
        jsch.addIdentity(new File(privateKeyFilename).getAbsolutePath(), password);
        session = jsch.getSession(username, "127.0.0.1", port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get(remoteFilename, localFilename);
        sftpChannel.exit();
        session.disconnect();
    }

    private void generateRsaKeyPair(String privateKeyFilename, String publicKeyFilename, String password) throws Exception {
        JSch jsch = new JSch();
        KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
        keyPair.writePrivateKey(privateKeyFilename, password.getBytes());
        keyPair.writePublicKey(publicKeyFilename, "sftp-maven-plugin@daknin.github.com");
        keyPair.dispose();
    }
}
