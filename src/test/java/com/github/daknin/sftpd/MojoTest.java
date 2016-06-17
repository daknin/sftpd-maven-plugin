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
import org.junit.Test;

import java.io.File;

/**
 * Created on 30/01/2016.
 */
public class MojoTest {
    @Test
    public void testName() throws Exception {
        String userDir = System.getProperty("user.dir");
        SftpdRunMojo startMojo = new SftpdRunMojo();
        Model model = new Model();
        MavenProject project = new MavenProject(model);
        startMojo.setMavenProject(project);
        startMojo.setServerRoot(new File(userDir + "/target/sftpd"));
        startMojo.setUsername("theUser");
        startMojo.setPassword("password");
        generateRsaKeyPair();
        startMojo.setAuthorisedKeysFile(new File(userDir + "/target/sftpd/authorized_keys"));
        startMojo.setServerKey(new File(userDir + "/target/sftpd/hostkey.pem"));
        startMojo.setPort(2223);
        startMojo.execute();

        sftpFileUsingPassword();
        sftpFileUsingPublicKey();

        SftpdStopMojo stopMojo = new SftpdStopMojo();
        stopMojo.setMavenProject(project);

        stopMojo.execute();

        //assertNull(mojo.getServer);
    }

    private void sftpFileUsingPassword() throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = null;
        session = jsch.getSession("theUser", "127.0.0.1", 2223);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword("password");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get("remotefile.txt", "target/localfilePassword.txt");
        sftpChannel.exit();
        session.disconnect();
    }

    private void sftpFileUsingPublicKey() throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = null;
        jsch.addIdentity(new File("target/id_rsa").getAbsolutePath(), "password");
        session = jsch.getSession("theUser", "127.0.0.1", 2223);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get("remotefile.txt", "target/localfileKey.txt");
        sftpChannel.exit();
        session.disconnect();
    }

    private void generateRsaKeyPair() throws Exception {
        String userDir = System.getProperty("user.dir");
        JSch jsch = new JSch();
        KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
        keyPair.writePrivateKey(userDir + "/target/id_rsa", "password".getBytes());
        keyPair.writePublicKey(userDir + "/target/sftpd/authorized_keys", "sftp-maven-plugin@daknin.github.com");
        keyPair.dispose();
    }
}
