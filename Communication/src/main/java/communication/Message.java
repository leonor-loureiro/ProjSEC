package communication;

import java.io.Serializable;

public class Message implements Serializable {


    private String operation;

    public Message(){
    }


    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

}