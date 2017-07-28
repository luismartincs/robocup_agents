package commlib.task.pf;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The ClearRouteTaskMessage represent the message that command to clear roads.<br>
 * This class instruct PF to secure a route from area A to area B.
 * 
 * @author takefumi
 * 
 */
public class ClearRouteTaskMessage extends PoliceForceTaskMessage {
	/**
	 * <h2>Constructor</h2> Create the message to order to clear route. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>pfID:EntityID of PF that is ordered to do.</li>
	 * <h2>Constructor</h2> Create the message to order fight fires to FB. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>ownerID:EntityID of the agent that sent this message.</li>
	 * <li>fbID:EntityID of FB that is ordered to do.</li>
	 * <li>departure:departure of the route</li>
	 * <li>destination:destination of the route</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param ownerID
	 *            EntityID of the agent that sent this message.
	 * @param pfID
	 *            EntityID of PF
	 * @param departure
	 *            EntityID of departure area
	 * @param destination
	 *            EntityID of the destination area
	 */
	public ClearRouteTaskMessage(int time, EntityID ownerID, EntityID pfID,
			EntityID departure, EntityID destination) {
		super(BaseMessageType.CLEAR_ROUTE, time, ownerID, pfID);
		super.setData(new EntityIDData(DataType.AREA, departure), 0);
		super.setData(new EntityIDData(DataType.AREA, destination), 1);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public ClearRouteTaskMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.CLEAR_ROUTE, bitList, offset, bitSizeMap);
	}

	/**
	 * EntityID of daparture area
	 * 
	 * @return 
	 *         EntityID of area
	 */
	public EntityID getDepartureAreaID() {
		return getID(DataType.AREA, 0);
	}

	/**
	 * EntityID of destination area
	 * 
	 * @return 
	 *         EntityID of area
	 */
	public EntityID getDestinationAreaID() {
		return getID(DataType.AREA, 1);
	}
}
