package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The UnpassableInformation show the information that some area is not
 * available to go to the neighbor area.
 * 
 * @author takefumi
 * 
 */
public class UnpassableInformation extends WorldInformation {
	/**
	 * <h2>Constructor</h2> Create the unpassable information.<br>
	 * Included data are follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>platoonID:EntityID of the rescue agent(pf,at,fb).</li>
	 * <li>from:EntityID of area that the agent is standing</li>
	 * <li>to:EntityID of destination area</li>
	 * <li>blockade:EntityID of blockade that is a cause of the unpassable
	 * situation</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param platoonID
	 *            EntityID of the rescue agent
	 * @param from
	 *            EntityID of area that the agent is standing.
	 * @param to
	 *            EntityID of destination area
	 * @param blockade
	 *            EntityID of blockade that is a cause of the unppasable
	 *            situation.
	 */
	public UnpassableInformation(int time, EntityID platoonID, EntityID from,
			EntityID to, EntityID blockade) {
		super(BaseMessageType.UNPASSABLE, time);
		super.setData(new EntityIDData(DataType.PLATOON_AGENT, platoonID));
		super.setData(new EntityIDData(DataType.AREA, from), 0);
		super.setData(new EntityIDData(DataType.AREA, to), 1);
		super.setData(new EntityIDData(DataType.BLOCKADE, blockade));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public UnpassableInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		this(BaseMessageType.UNPASSABLE, bitList, offset, bitSizeMap);
	}

	protected UnpassableInformation(BaseMessageType type,
			List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the rescue agent.
	 * 
	 * @return EntityID
	 */
	public EntityID getAgentID() {
		return super.getID(DataType.PLATOON_AGENT, 0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getAgentID();
	}

	/**
	 * Return EntityID of area that the agent is standing
	 * 
	 * @return 
	 *         EntityID of area
	 */
	public EntityID getFromAreaID() {
		return super.getID(DataType.AREA, 0);
	}

	/**
	 * EntityID of destination area
	 * 
	 * @return 
	 *         EntityID of area
	 */
	public EntityID getToAreaID() {
		return super.getID(DataType.AREA, 1);
	}

	/**
	 * EntityID of blockade that is a cause of the unppasable situation.
	 * 
	 * @return EntityID of blockade
	 */
	public EntityID getBLockadeID() {
		return super.getID(DataType.BLOCKADE, 0);
	}
}
