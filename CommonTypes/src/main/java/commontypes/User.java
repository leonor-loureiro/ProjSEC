package commontypes;

import java.io.Serializable;
import java.security.Key;

/**
 * This class represents a user
 */
public class User implements Serializable {
    String userID;
    Key publicKey;

    public String getUserID() {
        return userID;
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
}
