package commontypes;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * This class represents a user
 */
public class User implements Serializable {
    String userID;
    Key publicKey;
    int port;


    public User(String userID, int port) {
        this.userID = userID;
        this.port = port;
    }

    public User(String userID, int port, PublicKey publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
        this.port = port;
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

    public Key getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object obj) {
        User other = (User) obj;
        return obj.getClass().equals(User.class) && userID.equals(other.getUserID()) &&
                Arrays.equals(publicKey.getEncoded(), other.publicKey.getEncoded());
    }
}
