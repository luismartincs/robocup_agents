package commlib.report;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;
import commlib.message.RCRSCSMessage;

import rescuecore2.worldmodel.EntityID;

/**
 * The abstract class show the report message.
 * 
 * @author takefumi
 * 
 */
public abstract class ReportMessage extends RCRSCSMessage {
	public ReportMessage(BaseMessageType type, int time, EntityID platoonID) {
		super(type, time);
		super.setData(new EntityIDData(DataType.PLATOON_AGENT, platoonID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public ReportMessage(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the rescue agent that send this message.
	 * 
	 * @return 
	 *         EntityID of the rescue agent.
	 */
	public EntityID getAssignedAgentID() {
		return super.getID(DataType.PLATOON_AGENT, 0);
	}
}
