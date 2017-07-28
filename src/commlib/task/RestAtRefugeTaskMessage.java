package commlib.task;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;
@Deprecated
/**
 * @author takefumi
 * 
 */
public class RestAtRefugeTaskMessage extends TaskMessage {
	public RestAtRefugeTaskMessage(int time, EntityID ownerID,
			EntityID targetAgentID, EntityID refugeID) {
		super(BaseMessageType.REST_AT_REFUGE_TASK, time, ownerID);
		// this.setData(new ValueData(DataType.PLATOON_AGENT,
		// targetAgentID.getValue()));
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, targetAgentID));
		// this.setData(new ValueData(DataType.REFUGE, refugeID.getValue()));
		this.setData(new EntityIDData(DataType.REFUGE, refugeID));
	}

	public RestAtRefugeTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.REST_AT_REFUGE_TASK, bitList, offset, bitSizeMap);
	}

	public EntityID getRefugeID() {
		return super.getID(DataType.REFUGE, 0);
	}
}
