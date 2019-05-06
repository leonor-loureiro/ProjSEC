package communication;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * This class represents a user
 */
public class ServerInfo implements Serializable {
    private int port;
    private String address;
    private PublicKey publicKey;

    public ServerInfo(String address, int port, PublicKey publicKey) {
        this.port = port;
        this.address = address;
        this.publicKey = publicKey;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getID(){
        return address + port;
    }

    @Override
    public boolean equals(Object obj) {
        ServerInfo other = (ServerInfo) obj;
        return obj.getClass().equals(ServerInfo.class) && getID().equals(other.getID()) &&
                Arrays.equals(publicKey.getEncoded(), other.publicKey.getEncoded());
    }

}
