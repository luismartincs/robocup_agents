package commlib.task;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The DecideLeaderTaskMessage represent the message that decide a leader(such as Center).<br>
 * Please use this message when in the case of Centerless.

 * @author shogo horibe
 * 
 */
public class DecideLeaderTaskMessage extends TaskMessage{

	/**
	 * <h2>Constructor</h2> Create the message to decide leader to Platoon Agents. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>targetAgentID:EntityID of agent that is ordered to do.</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message.
	 * @param targetAgentID
	 *            EntityID of agent.
	 */	
	public DecideLeaderTaskMessage(int time, EntityID ownerID, EntityID targetAgentID){
		super(BaseMessageType.DECIDE_LEADER, time, ownerID);
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, targetAgentID));
	}
	
	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public DecideLeaderTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.DECIDE_LEADER, bitList, offset, bitSizeMap);
	}
}
