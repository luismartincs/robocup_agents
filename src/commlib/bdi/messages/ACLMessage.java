package commlib.bdi.messages;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.RCRSCSData;
import commlib.data.ValueData;
import commlib.information.WorldInformation;
import commlib.message.BaseMessageType;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;
import java.util.EnumMap;
import java.util.List;

public class ACLMessage extends WorldInformation{

    public ACLMessage(int time, EntityID sender, ACLPerformative performative,EntityID receiver) {
        super(BaseMessageType.ACL_MESSAGE, time);

        this.setData(new ValueData(DataType.ALL_ENTITIES, sender.getValue()),0);
        this.setData(new ValueData(DataType.ALL_ENTITIES, receiver.getValue()),1);
        this.setData(new ValueData(DataType.PERFORMATIVE, performative.getValue()));
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
        int res = -1;
        RCRSCSData<?> d = this.getData(DataType.ALL_ENTITIES, 0);
        if (d != null) {
            Integer id = ((ValueData) d).getData();
            if (id != null) {
                res = id;
            }
        }
        return res;
    }

    public int getReceiver(){
        int res = -1;
        RCRSCSData<?> d = this.getData(DataType.ALL_ENTITIES, 1);
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
