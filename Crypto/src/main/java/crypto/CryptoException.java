package crypto;

public class CryptoException extends Exception {
    private String message;

    public CryptoException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}