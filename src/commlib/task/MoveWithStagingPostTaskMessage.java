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
public class MoveWithStagingPostTaskMessage extends TaskMessage {
	public MoveWithStagingPostTaskMessage(int time, EntityID ownerID,
			EntityID targetAgentID, EntityID destAreaID,
			List<EntityID> stagingPointList) {
		super(BaseMessageType.MOVE_WITH_STAGING_POST_TASK, time, ownerID);
		// this.setData(new ValueData(DataType.PLATOON_AGENT,
		// targetAgentID.getValue()));
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, targetAgentID));
		// this.setData(new ValueData(DataType.AREA, destAreaID.getValue()));
		this.setData(new EntityIDData(DataType.AREA, destAreaID));
		this.setEntityIDListData(DataType.AREA_LIST, stagingPointList);
	}

	public MoveWithStagingPostTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.MOVE_WITH_STAGING_POST_TASK, bitList, offset,
				bitSizeMap);
	}

	public EntityID getDestinationAreaID() {
		return super.getID(DataType.AREA, 0);
	}

	public List<EntityID> getStagingAreaIDs() {
		return super.getEntityIDList(DataType.AREA_LIST, 0);
	}
}
