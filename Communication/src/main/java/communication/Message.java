package communication;

import java.io.Serializable;

public class Message implements Serializable {

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String content;

    public Message(){
    }

}