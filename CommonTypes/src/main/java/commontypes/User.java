package commontypes;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.security.Key;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * This class represents a user
 */
public class User implements Serializable {
    String userID;
    PublicKey publicKey;

    public User(String userID, PublicKey publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
    }

    public String getUserID() {
        return userID;
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

    @Override
    public boolean equals(Object obj) {
        User other = (User) obj;
        return obj.getClass().equals(User.class) && userID.equals(other.getUserID()) &&
                Arrays.equals(publicKey.getEncoded(), other.publicKey.getEncoded());
    }
}
