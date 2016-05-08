import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair

def folder = new File(basedir, '/src/test/resources/')
if( !folder.exists() ) {
    folder.mkdirs()
}

def String basedirName = basedir;
JSch jsch = new JSch()
KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA)
keyPair.writePrivateKey(basedirName + "/src/test/resources/id_rsa", "password".getBytes())
keyPair.writePublicKey(basedirName + "/src/test/resources/authorized_keys", "sftp-maven-plugin@daknin.github.com")
keyPair.dispose()
