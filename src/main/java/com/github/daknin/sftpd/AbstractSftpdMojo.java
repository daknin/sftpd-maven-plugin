package com.github.daknin.sftpd;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

public abstract class AbstractSftpdMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject mavenProject;

    @Parameter(property = "serverRoot", defaultValue = "${project.build.directory}/sftpd/")
    protected File serverRoot;

    @Parameter(property = "username", defaultValue = "user")
    protected String username;

    @Parameter(property = "password")
    protected String password;

    @Parameter(property = "authorisedKeysFile")
    protected File authorisedKeysFile;

    @Parameter(property = "serverKey", defaultValue="hostkey.ser")
    protected File serverKey;

    @Parameter(property = "port", defaultValue = "2121")
    protected int port;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    public File getServerRoot() {
        return serverRoot;
    }

    public void setServerRoot(File serverRoot) {
        this.serverRoot = serverRoot;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public void setMavenProject(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public File getAuthorisedKeysFile() {
        return authorisedKeysFile;
    }

    public void setAuthorisedKeysFile(File authorisedKeysFile) {
        this.authorisedKeysFile = authorisedKeysFile;
    }

    public File getServerKey() {
        return serverKey;
    }

    public void setServerKey(File serverKey) {
        this.serverKey = serverKey;
    }
}
