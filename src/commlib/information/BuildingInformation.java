package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.ValueData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The BuildingInformation show the building information.
 * 
 * @author takefumi
 * 
 */
public class BuildingInformation extends WorldInformation {

	/**
	 * <h2>Constructor</h2> Create the inforamtion of building.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>buildingID:EntityID of the building</li>
	 * <li>fieryness:fieryness of the building</li>
	 * <li>brokenness:brokenness of the building</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param buildingId
	 *            EntityiD of the building
	 * @param fieryness
	 *            fieryness of the building
	 * @param brokenness
	 *            blockenness of the building
	 */
	public BuildingInformation(int time, EntityID buildingId, int fieryness,
			int brokenness) {
		super(BaseMessageType.BUILDING, time);
		// this.setData(new ValueData(DataType.BUILDING, id.getValue()));
		this.setData(new EntityIDData(DataType.BUILDING, buildingId));
		this.setData(new ValueData(DataType.FIERYNESS, fieryness));
		this.setData(new ValueData(DataType.BROKENNESS, brokenness));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public BuildingInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.BUILDING, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the building.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getBuildingID() {
		return super.getID(DataType.BUILDING, 0);
	}

	/**
	 * Return brokenness of the building.
	 * 
	 * @return 
	 *         brokenness
	 */
	public int getBrokenness() {
		return super.getBrokenness(0);
	}

	/**
	 * Return fieryness of the building.
	 * 
	 * @return 
	 *         fieryness
	 */
	public int getFieryness() {
		return super.getFieryness(0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getBuildingID();
	}
}
