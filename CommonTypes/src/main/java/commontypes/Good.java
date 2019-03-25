package commontypes;

import java.io.Serializable;

/**
 * This class represents a good
 */
public class Good implements Serializable {
    String goodID;
    String userID;
    boolean onSale;

    public Good(String goodID, String userID, boolean onSale) {
        this.goodID = goodID;
        this.userID = userID;
        this.onSale = onSale;
    }

    public String getGoodID() {
        return goodID;
    }

    public void setGoodID(String goodID) {
        this.goodID = goodID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    @Override
    public boolean equals(Object obj) {
        Good other = (Good) obj;
        return goodID.equals(other.goodID) && userID.equals(other.userID) && onSale == other.isOnSale();
    }
}
