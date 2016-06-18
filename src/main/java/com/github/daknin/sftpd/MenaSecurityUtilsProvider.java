package com.github.daknin.sftpd;

import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;

import java.nio.file.Path;

/**
 * Created on 18/06/2016.
 */
public class MenaSecurityUtilsProvider implements SecurityUtilsProvider {
    public AbstractGeneratorHostKeyProvider createGeneratorHostKeyProvider(Path path) {
        return SecurityUtils.createGeneratorHostKeyProvider(path);
    }
}
