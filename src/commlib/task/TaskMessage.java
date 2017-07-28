package commlib.task;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.EntityIDListData;
import commlib.message.BaseMessageType;
import commlib.message.RCRSCSMessage;

import rescuecore2.worldmodel.EntityID;

/**
 * The TaskMessage is the message class express the task.
 * 
 * @author takefumi
 * 
 */
public abstract class TaskMessage extends RCRSCSMessage implements ITaskMessage {
	/**
	 * <h2>Constructor</h2>Create the task.
	 * 
	 * @param type
	 * @param time
	 * @param ownerID
	 */
	public TaskMessage(BaseMessageType type, int time, EntityID ownerID) {
		super(type, time);
		this.setData(new EntityIDData(DataType.RESCUE_AGENT, ownerID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public TaskMessage(BaseMessageType type, List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	@Deprecated
	protected void setEntityIDListData(DataType dType, List<EntityID> list) {
		super.setData(new EntityIDListData(dType, list));
	}

	/**
	 * Return EntityID of rescue agent that have to execute this task.
	 * 
	 * 
	 * @return 
	 *         EntityID of rescue agent
	 */
	public EntityID getAssignedAgentID() {
		return super.getID(DataType.PLATOON_AGENT, 0);
	}

	/**
	 * Return EntityID of the agent that sent this message.
	 * 
	 * @return 
	 *         EntityID of the agent(at, ac, pf, po, fb, fs)
	 */
	public EntityID getMessageOwnerID() {
		return super.getID(DataType.RESCUE_AGENT, 0);
	}
}
