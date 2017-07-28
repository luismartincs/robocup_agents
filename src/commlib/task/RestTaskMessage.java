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
public class RestTaskMessage extends TaskMessage {
	public RestTaskMessage(int time, EntityID ownerID, EntityID targetAgentID) {
		super(BaseMessageType.REST_TASK, time, ownerID);
		// this.setData(new ValueData(DataType.PLATOON_AGENT,
		// targetAgentID.getValue()));
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, targetAgentID));
	}

	public RestTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.REST_TASK, bitList, offset, bitSizeMap);
	}

}