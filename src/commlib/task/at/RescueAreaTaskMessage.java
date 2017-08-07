package commlib.task.at;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDListData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The RescueAreaTaskMessage show the message that command to rescue.<br>
 * This class inform some areas (set of area) and order to search and rescue
 * victim in this area.
 * 
 * 
 * @author takefumi
 * 
 */
public class RescueAreaTaskMessage extends AmbulanceTeamTaskMessage {
	/**
	 * <h2>Constructor</h2> Create the message to order to rescue to AT. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>atID:EntityID of AT that is ordered to do.</li>
	 * <li>areas:EntityIDs of area that are search target</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message.
	 * @param atID
	 *            EntityID of AT.
	 * @param areas
	 *            EntityIDs of area
	 */
	public RescueAreaTaskMessage(int time, EntityID ownerID, EntityID atID,
			EntityID... areas) {
		this(time, ownerID, atID, Arrays.asList(areas));
	}

	/**
	 * <h2>Constructor</h2> Create the message to order rescue to AT. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>atID:EntityID of AT that is ordered to do.</li>
	 * <li>areas:List of EntityIDs of area that are search target</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message
	 * @param atID
	 *            EntityID of the AT
	 * @param areas
	 *            List of EntityID
	 */
	public RescueAreaTaskMessage(int time, EntityID ownerID, EntityID atID,
			List<EntityID> areas) {
		super(BaseMessageType.RESCUE_AREA, time, ownerID, atID);
		this.setData(new EntityIDListData(DataType.AREA_LIST, areas));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public RescueAreaTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.RESCUE_AREA, bitList, offset, bitSizeMap);
	}

	/**
	 * Return the list of areas that are target of the search.
	 * 
	 * @return 
	 *         Return list of areas({@literal List<EntityID>})
	 */
	public List<EntityID> getTargetAreaList() {
		return super.getEntityIDList(DataType.AREA_LIST, 0);
	}

}
