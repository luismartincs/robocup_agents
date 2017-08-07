package commlib.task.pf;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;
import commlib.task.TaskMessage;

import rescuecore2.worldmodel.EntityID;

/**
 * The class represent the Task for Police Force agent.
 * 
 * @author takefumi
 * 
 */
public abstract class PoliceForceTaskMessage extends TaskMessage {
	/**
	 * <h2>Constructor</h2> Create the message to give instructions to PF.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>pfID:EntityID of PF that is ordered to do.</li>
	 * </ul>
	 * 
	 * @param type
	 *            message type
	 * @param time
	 *            step num
	 * @param pfID
	 *            EntityID of PF
	 */
	public PoliceForceTaskMessage(BaseMessageType type, int time,
			EntityID ownerID, EntityID pfID) {
		super(type, time, ownerID);
		// this.setData(new ValueData(DataType.POLICE_FORCE, pfID.getValue()));
		this.setData(new EntityIDData(DataType.POLICE_FORCE, pfID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public PoliceForceTaskMessage(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of PF that have to execute this task.
	 * 
	 * @return 
	 *         EntityID of PF
	 */
	public EntityID getAssignedAgentID() {
		return super.getID(DataType.POLICE_FORCE, 0);
	}
}
