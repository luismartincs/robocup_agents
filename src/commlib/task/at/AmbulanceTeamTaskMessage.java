package commlib.task.at;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;
import commlib.task.TaskMessage;

import rescuecore2.worldmodel.EntityID;

/**
 * The class represent the Task for ambulance team agent.
 * 
 * @author takefumi
 * 
 */
public abstract class AmbulanceTeamTaskMessage extends TaskMessage {
	/**
	 * <h2>Constructor</h2> Create the message to give instructions to AT.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>atID:EntityID of AT that is ordered to do.</li>
	 * </ul>
	 * 
	 * @param type
	 *            message type
	 * @param time
	 *            step num
	 * @param atID
	 *            EntityID of AT
	 */
	public AmbulanceTeamTaskMessage(BaseMessageType type, int time,
			EntityID ownerID, EntityID atID) {
		super(type, time, ownerID);
		this.setData(new EntityIDData(DataType.AMBULANCE_TEAM, atID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public AmbulanceTeamTaskMessage(BaseMessageType type,
			List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of AT that have to execute this task.
	 * 
	 * @return 
	 *         EntityID of AT
	 */
	public EntityID getAssignedAgentID() {
		return super.getID(DataType.AMBULANCE_TEAM, 0);
	}

}
