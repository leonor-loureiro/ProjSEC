package commontypes;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * This class represents a user
 */
public class User implements Serializable {
    String userID;
    String address;
    PublicKey publicKey;
    PrivateKey privateKey;
    int port;

    public User(String userID) {
        this.userID = userID;
    }

    public User(String userID, int port) {
        this.userID = userID;
        this.port = port;
    }

    public User(String userID, int port, PublicKey publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
        this.port = port;
    }

    public User(String userID, int port,PrivateKey privateKey, PublicKey publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
        this.port = port;
        this.privateKey = privateKey;
    }

    public String getUserID() {
        return userID;
    }

    public int getPort() {
        return port;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPrivateKey(PrivateKey privateKey) {this.privateKey = privateKey;}


    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    @Override
    public boolean equals(Object obj) {
        User other = (User) obj;
        return obj.getClass().equals(User.class) && userID.equals(other.getUserID()) &&
                Arrays.equals(publicKey.getEncoded(), other.publicKey.getEncoded());
    }

}
