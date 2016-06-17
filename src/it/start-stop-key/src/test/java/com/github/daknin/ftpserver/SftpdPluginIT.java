package com.github.daknin.ftpserver.plugin;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.junit.Test;
import java.io.File;

public class SftpdPluginIT {
    @Test
    public void sftpUsingKey() throws Exception {
        Integer port = Integer.valueOf(System.getProperty("sftpd.port"));

        JSch jsch = new JSch();
        Session session = null;
        File idRsaFile = new File(this.getClass().getResource("/id_rsa").toURI());
//        jsch.addIdentity(new File("id_rsa").getAbsolutePath(), "password");
        jsch.addIdentity(idRsaFile.getAbsolutePath(), "password");
        session = jsch.getSession("theUser", "127.0.0.1", port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get("remotefile.txt", "target/localfileKey.txt");
        sftpChannel.exit();
        session.disconnect();

    }
}
