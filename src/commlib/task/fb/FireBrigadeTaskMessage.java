package commlib.task.fb;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;
import commlib.task.TaskMessage;

import rescuecore2.worldmodel.EntityID;

/**
 * The class represent the Task for Fire Brigade agent.
 * 
 * @author takefumi
 * 
 */
public abstract class FireBrigadeTaskMessage extends TaskMessage {
	/**
	 * <h2>Constructor</h2> Create the message to give instructions to FB.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>fbID:EntityID of FB that is ordered to do.</li>
	 * </ul>
	 * 
	 * @param type
	 *            message type
	 * @param time
	 *            step num
	 * @param fbID
	 *            EntityID of FB
	 */
	public FireBrigadeTaskMessage(BaseMessageType type, int time,
			EntityID ownerID, EntityID fbID) {
		super(type, time, ownerID);
		// this.setData(new ValueData(DataType.POLICE_FORCE, fbID.getValue()));
		this.setData(new EntityIDData(DataType.FIRE_BRIGADE, fbID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public FireBrigadeTaskMessage(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of FB that have to execute this task.
	 * 
	 * @return 
	 *         EntityID of FB
	 */
	public EntityID getAssignedAgentID() {
		return super.getID(DataType.FIRE_BRIGADE, 0);
	}

}
