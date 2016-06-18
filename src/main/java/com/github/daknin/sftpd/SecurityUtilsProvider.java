package com.github.daknin.sftpd;

import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;

import java.nio.file.Path;

/**
 * Created on 18/06/2016.
 */
public interface SecurityUtilsProvider {
    AbstractGeneratorHostKeyProvider createGeneratorHostKeyProvider(Path path);
}
