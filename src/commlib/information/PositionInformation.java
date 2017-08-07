package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.message.BaseMessageType;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;

/**
 * The class represent the location information of rescue agent.
 * 
 * @author takefumi
 * 
 */
public class PositionInformation extends WorldInformation {
	/**
	 * <h2>Constructor</h2> Create the information of the agent's location.<br>
	 * Included data are follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>agentID:EntityID of the rescue agent.</li>
	 * <li>coordinate:agent location.({@literal Pair<Integer,Integer>})</li>
	 * </ul>
	 * 
	 * @param time
	 *            
	 *            step num
	 * @param platoonID
	 *            
	 *            EntityID of the rescue agent
	 * @param cor
	 *            agent location
	 */
	public PositionInformation(int time, EntityID platoonID,
			Pair<Integer, Integer> cor) {
		super(BaseMessageType.POSITION, time);
		super.setData(new EntityIDData(DataType.PLATOON_AGENT, platoonID));
		super.setCoorinate(cor);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public PositionInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.POSITION, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the rescue agent.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getAgentID() {
		return super.getID(DataType.PLATOON_AGENT, 0);
	}

	/**
	 * Return the coordinate of the agent.
	 * 
	 * @return 
	 *         coordinate({@literal Pair<Integer,Integer>})
	 */
	public Pair<Integer, Integer> getCoordinate() {
		return super.getCoodinate(0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getAgentID();
	}
}
