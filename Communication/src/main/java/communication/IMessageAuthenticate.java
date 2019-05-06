package communication;

import crypto.CryptoException;

public interface IMessageAuthenticate {

    void  authenticate(Message message) throws CryptoException;

    boolean isValid(Message message) throws SaveNonceException, CryptoException;
}
