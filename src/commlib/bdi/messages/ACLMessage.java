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

    public ACLMessage(int time, EntityID platoonID, ACLPerformative performative) {
        super(BaseMessageType.ACL_MESSAGE, time);
        super.setData(new EntityIDData(DataType.CENTER_AGENT, platoonID));
        this.setData(new ValueData(DataType.PERFORMATIVE,performative.getValue()));
    }

    public ACLMessage(List<Integer> bitList, int offset, EnumMap<DataType, Integer> bitSizeMap) {
        super(BaseMessageType.ACL_MESSAGE, bitList, offset, bitSizeMap);
    }

    public EntityID getAgentID() {
        return super.getID(DataType.CENTER_AGENT, 0);
    }


    public EntityID getEntityID() {
        return this.getAgentID();
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
