import static org.junit.Assert.assertTrue

Properties properties = new Properties()
def portsFile = new File(basedir, "target/sftpd/ports.txt")
portsFile.withInputStream {
    properties.load(it)
}

def file = new File(basedir, "build.log")
assertTrue "SFTP Server should have started server on port ${properties['sftpd.port']}", file.text.contains("Started SFTP Server on port ${properties['sftpd.port']}")

assertTrue 'Shutdown should have been invoked', file.text.contains("SFTP server stopped.")
