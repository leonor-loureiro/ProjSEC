package communication;

public class ByzantineSimulator {
    public static Boolean isByzantine = false;

    public static Boolean getByzantine(){
        return isByzantine;
    }

    public static void setByzantine(Boolean isByzantine){
        ByzantineSimulator.isByzantine = isByzantine;
    }

}
