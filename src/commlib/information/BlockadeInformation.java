package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.ValueData;
import commlib.message.BaseMessageType;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;

/**
 * The BlockadeInformation represent the Information of Blockade.
 * 
 * @author takefumi
 * 
 */
public class BlockadeInformation extends WorldInformation {

	/**
	 * <h2>Constructor</h2> Create the blockade information.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>blockadeID:EntityID of the blockade</li>
	 * <li>roadID:EntityID of road that the blockade exist</li>
	 * <li>repairCost:repair cost of the blockade</li>
	 * </ul>
	 * Additionally, This message can send the barycentric coodinate of the
	 * blockade, if coodinate information doesn't exist, we get coodinate
	 * information as (-1,-1).
	 * 
	 * @param time
	 *            step num
	 * @param blockadeID
	 *            EntityID of the blockade
	 * @param roadID
	 *            EntityID of road that the blockade exists
	 * @param repairCost
	 *            repair cost of the blockade
	 */
	public BlockadeInformation(int time, EntityID blockadeID, EntityID roadID,
			int repairCost) {
		super(BaseMessageType.BLOCKADE, time);
		// this.setData(new ValueData(DataType.BLOCKADE, id.getValue()));
		this.setData(new EntityIDData(DataType.BLOCKADE, blockadeID));
		// this.setData(new ValueData(DataType.ROAD, road.getValue()));
		this.setData(new EntityIDData(DataType.ROAD, roadID));
		this.setData(new ValueData(DataType.REPAIR_COST, repairCost));
		this.setCoorinate(new Pair<Integer, Integer>(-1, -1));
	}

	/**
	 * <h2>Constructor</h2> Create the blockade information.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>blockadeID:EntityID of the blockade</li>
	 * <li>roadID:EntityID of road that the blockade exist</li>
	 * <li>repairCost:repair cost of the blockade</li>
	 * <li>blockadeCor:barycentric coordinate of the blockade.</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param blockadeID
	 *            EntityID of the blockade
	 * @param roadID
	 *            EntityiD of road that the blockade exists
	 * @param repairCost
	 *            repair cost of the blockade
	 * @param blockadeCor
	 *            barycentric coordinate of the blockade
	 */
	public BlockadeInformation(int time, EntityID blockadeID, EntityID roadID,
			int repairCost, Pair<Integer, Integer> blockadeCor) {
		// this(time, blockadeID, roadID, repairCost);
		super(BaseMessageType.BLOCKADE_WITH_COORDINATE, time);
		// this.setData(new ValueData(DataType.BLOCKADE, id.getValue()));
		this.setData(new EntityIDData(DataType.BLOCKADE, blockadeID));
		// this.setData(new ValueData(DataType.ROAD, road.getValue()));
		this.setData(new EntityIDData(DataType.ROAD, roadID));
		this.setData(new ValueData(DataType.REPAIR_COST, repairCost));
		super.setCoorinate(blockadeCor);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public BlockadeInformation(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the blockade.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getBlockadeID() {
		return super.getID(DataType.BLOCKADE, 0);
	}

	/**
	 * Return EntityID of road that the blockade exist.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getRoadID() {
		return super.getID(DataType.ROAD, 0);
	}

	/**
	 * Return repair cost of the blockade.
	 * 
	 * @return 
	 *         repair cost
	 */
	public int getRepairCost() {
		return super.getRepairCost(0);
	}

	/**
	 * Return the coordinate of the blockade.
	 * 
	 * @return 
	 *         barycentric coordinate({@literal Pair<Integer,Integer>})
	 * 
	 */
	public Pair<Integer, Integer> getCoodinate() {
		return super.getCoodinate(0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getBlockadeID();
	}
}
