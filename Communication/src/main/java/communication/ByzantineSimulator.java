package communication;

public class ByzantineSimulator {
    public static Boolean isByzantine = false;

    public static Boolean isDoubleBroadcast = false;

    public static Boolean getByzantine(){
        return isByzantine;
    }

    public static void setByzantine(Boolean isByzantine){
        ByzantineSimulator.isByzantine = isByzantine;
    }

    public static Boolean getIsDoubleBroadcast() {
        return isDoubleBroadcast;
    }

    public static void setIsDoubleBroadcast(Boolean isDoubleBroadcast) {
        ByzantineSimulator.isDoubleBroadcast = isDoubleBroadcast;
    }
}
