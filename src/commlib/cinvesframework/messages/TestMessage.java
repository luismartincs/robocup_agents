package commlib.cinvesframework.messages;

import java.io.Serializable;

public class TestMessage implements Serializable{

    private int sender;
    private String content = "";

    public TestMessage(int sender,String content){
        this.sender = sender;
        this.content = content;
    }

    public int getSender(){
        return this.sender;
    }
    public String getContent(){
        return this.content;
    }
}
