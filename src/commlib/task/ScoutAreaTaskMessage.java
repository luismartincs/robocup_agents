package commlib.task;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.EntityIDListData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The ScoutAreaTaskMessage represent the message that command to scout.<br>
 * This class inform some areas (set of area) to Platoon Agents and order to search and
 * scout in this area.
 * 
 * @author shogo horibe
 * 
 */
public class ScoutAreaTaskMessage extends TaskMessage {
	
	/**
	 * <h2>Constructor</h2> Create the message to order to scout to Platoon Agents. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>targetAgentID:EntityID of agent that is ordered to do.</li>
	 * <li>areas:EntityIDs of area that are scout</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message.
	 * @param targetAgentID
	 *            EntityID of agent.
	 * @param areas
	 *            EntityIDs of area
	 */	
	public ScoutAreaTaskMessage(int time, EntityID ownerID, EntityID targetAgentID, EntityID... areas) {
		this(time, ownerID, targetAgentID, Arrays.asList(areas));
	}
	
	/**
	 * <h2>Constructor</h2> Create the message to order to scout to Platoon Agents. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>targetAgentID:EntityID of agent that is ordered to do.</li>
	 * <li>areas:List of EntityIDs of area that are scout</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message
	 * @param targetAgentID
	 *            EntityID of agent.
	 * @param areas
	 *            List of EntityID
	 */
	public ScoutAreaTaskMessage(int time, EntityID ownerID, EntityID targetAgentID, List<EntityID> areas){
		super(BaseMessageType.SCOUT_AREA, time, ownerID);
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, targetAgentID));
		this.setData(new EntityIDListData(DataType.AREA_LIST, areas));
	}
	
	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public ScoutAreaTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.SCOUT_AREA, bitList, offset, bitSizeMap);
	}

	/**
	 * Return list of EntityID of areas that are target of scout
	 * 
	 * @return 
	 *         List of fire fight targets({@literal List<EntityID>})
	 */
	public List<EntityID> getTargetAreaList() {
		return super.getEntityIDList(DataType.AREA_LIST, 0);
	}		
}