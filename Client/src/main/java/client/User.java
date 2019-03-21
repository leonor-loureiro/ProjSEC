package client;

import java.security.PrivateKey;
import java.security.PublicKey;

public class User {

    private String username;
    private char[] password;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public User() {
    }

    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, char[] password, PrivateKey privateKey, PublicKey publicKey) {
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() { return privateKey; }

    public void setPrivateKey(PrivateKey privateKey) { this.privateKey = privateKey; }

    public char[] getPassword() { return password; }

    public void setPassword(char[] password) { this.password = password; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public PublicKey getPublicKey() { return publicKey; }

    public void setPublicKey(PublicKey publicKey) { this.publicKey = publicKey; }


}
