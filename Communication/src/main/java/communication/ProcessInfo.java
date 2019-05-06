package communication;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * This class represents a user
 */
public class ProcessInfo implements Serializable {
    private String ID;
    private int port;
    private String host;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public ProcessInfo(String host, int port, PublicKey publicKey) {
        this.ID = host + port;
        this.port = port;
        this.host = host;
        this.publicKey = publicKey;
    }

    public ProcessInfo(String ID, PrivateKey privateKey) {
        this.ID = ID;
        this.privateKey = privateKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getID(){
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        ProcessInfo other = (ProcessInfo) obj;
        return obj.getClass().equals(ProcessInfo.class) && getID().equals(other.getID()) &&
                Arrays.equals(publicKey.getEncoded(), other.publicKey.getEncoded());
    }

}
