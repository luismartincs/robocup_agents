package commlib.cinvesframework.messages;

import commlib.data.DataType;
import commlib.data.RCRSCSData;
import commlib.data.ValueData;
import commlib.information.WorldInformation;
import commlib.message.BaseMessageType;
import rescuecore2.worldmodel.EntityID;
import java.util.EnumMap;
import java.util.List;

public class ACLMessage extends WorldInformation{

    public ACLMessage(int time, EntityID sender, ACLPerformative performative,EntityID receiver,int conversationId,int contentId) {
        super(BaseMessageType.ACL_MESSAGE, time);

        this.setData(new ValueData(DataType.ALL_ENTITIES, sender.getValue()),0);
        this.setData(new ValueData(DataType.ALL_ENTITIES, receiver.getValue()),1);
        this.setData(new ValueData(DataType.PERFORMATIVE, performative.getValue()));
        this.setData(new ValueData(DataType.CONVERSATION_ID,conversationId));
        this.setData(new ValueData(DataType.CONTENT,contentId),0);
       // this.setData(new EntityIDData(DataType.AREA, targetAgent));


    }

    public ACLMessage(int time, EntityID sender, ACLPerformative performative,EntityID receiver,int conversationId,int contentId, int xPosition, int yPosition, int victims, int injured) {
        super(BaseMessageType.ACL_MESSAGE, time);

        this.setData(new ValueData(DataType.ALL_ENTITIES, sender.getValue()),0);
        this.setData(new ValueData(DataType.ALL_ENTITIES, receiver.getValue()),1);
        this.setData(new ValueData(DataType.PERFORMATIVE, performative.getValue()));
        this.setData(new ValueData(DataType.CONVERSATION_ID,conversationId));
        this.setData(new ValueData(DataType.X_COORDINATE,xPosition));
        this.setData(new ValueData(DataType.Y_COORDINATE,yPosition));
        this.setData(new ValueData(DataType.CONTENT,contentId),0);
        this.setData(new ValueData(DataType.VICTIMS_NUMBER,victims));
        this.setData(new ValueData(DataType.INJURED_NUMBER,injured));
        // this.setData(new EntityIDData(DataType.AREA, targetAgent));


    }

    public ACLMessage(List<Integer> bitList, int offset, EnumMap<DataType, Integer> bitSizeMap) {
        super(BaseMessageType.ACL_MESSAGE, bitList, offset, bitSizeMap);
    }

    public EntityID getAgentID() {
        return super.getID(DataType.ALL_ENTITIES, 0);
    }


    public EntityID getEntityID() {
        return this.getAgentID();
    }

    public int getSender(){
       return getValueData(DataType.ALL_ENTITIES,0);
    }

    public int getReceiver(){
        return getValueData(DataType.ALL_ENTITIES,1);
    }

    public int getConversationId(){
        return getValueData(DataType.CONVERSATION_ID,0);
    }

    public int getContent(){
        return getValueData(DataType.CONTENT,0);
    }

    public int getXPosition(){ return getValueData(DataType.X_COORDINATE,0);}

    public int getYPosition(){ return getValueData(DataType.Y_COORDINATE,0);}

    public int getVictims(){ return getValueData(DataType.VICTIMS_NUMBER,0);}

    public int getInjured(){ return getValueData(DataType.INJURED_NUMBER,0);}

    private int getValueData(DataType dataType,int index){
        int res = -1;
        RCRSCSData<?> d = this.getData(dataType,index);
        if (d != null) {
            Integer id = ((ValueData) d).getData();
            if (id != null) {
                res = id;
            }
        }
        return res;
    }


    public ACLPerformative getPerformative(){
        int res = -1;
        RCRSCSData<?> d = this.getData(DataType.PERFORMATIVE, 0);
        if (d != null) {
            Integer id = ((ValueData) d).getData();
            if (id != null) {
                res = id;
            }
        }
        return ACLPerformative.createPerformative(res);
    }
}
