package com.github.daknin.ftpserver.plugin;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.junit.Test;

public class SftpdPluginIT {
    @Test
    public void sftpUsingPassword() throws Exception {
        Integer port = Integer.valueOf(System.getProperty("sftpd.port"));

        JSch jsch = new JSch();
        Session session = null;
        session = jsch.getSession("user", "127.0.0.1", port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword("passwordXX");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get("remotefile.txt", "target/localfilePassword.txt");
        sftpChannel.exit();
        session.disconnect();

    }
}
