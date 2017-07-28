package commlib.task.fb;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDListData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The ExtinguishAreaTaskMessage represent the message that command to
 * extinguish.<br>
 * This class inform some areas (set of area) to FB and order to search and
 * fight fires in this area.
 * 
 * @author takefumi
 * 
 */
public class ExtinguishAreaTaskMessage extends FireBrigadeTaskMessage {
	/**
	 * <h2>Constructor</h2> Create the message to order to fight fires to FB. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>fbID:EntityID of FB that is ordered to do.</li>
	 * <li>areas:EntityIDs of area that are search target</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message.
	 * @param fbID
	 *            EntityID of FB.
	 * @param areas
	 *            EntityIDs of area
	 */
	public ExtinguishAreaTaskMessage(int time, EntityID ownerID, EntityID fbID,
			EntityID... areas) {
		this(time, ownerID, fbID, Arrays.asList(areas));
	}

	/**
	 * <h2>Constructor</h2> Create the message to order fight fires to FB. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>fbID:EntityID of FB that is ordered to do.</li>
	 * <li>areas:List of EntityIDs of area that are search target</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message
	 * @param fbID
	 *            EntityID of the FB
	 * @param areas
	 *            List of EntityID
	 */
	public ExtinguishAreaTaskMessage(int time, EntityID ownerID, EntityID fbID,
			List<EntityID> areas) {
		super(BaseMessageType.EXTINGUISH_AREA, time, ownerID, fbID);
		super.setData(new EntityIDListData(DataType.AREA_LIST, areas));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public ExtinguishAreaTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.EXTINGUISH_AREA, bitList, offset, bitSizeMap);
	}

	/**
	 * Return list of EntityID of areas that are target of fire fight
	 * 
	 * @return 
	 *         List of fire fight targets({@literal List<EntityID>})
	 */
	public List<EntityID> getTargetAreaList() {
		return super.getEntityIDList(DataType.AREA_LIST, 0);
	}
}
