package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.ValueData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * This class show the Fire Brigade information.
 * 
 * @author takefumi
 * 
 */
public class FireBrigadeInformation extends WorldInformation {

	/**
	 * <h2>Constructor</h2> Create the information of fire brigade<br>
	 * Included data are follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>fbID:EntityID of the fire brigade</li>
	 * <li>hp:hp of the fb</li>
	 * <li>damage:damage of the fb</li>
	 * <li>buriedness:buriedness of the fb</li>
	 * <li>water:amount of left water</li>
	 * <li>areaID:EntityID of area that the fb is standing</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param fbID
	 *            EntityID
	 * @param hp
	 *            hp of the fb
	 * @param damage
	 *            damage of the fb
	 * @param buriedness
	 *            buriedness of the fb
	 * @param water
	 *            amount of left water
	 * @param areaID
	 *            EntityID of area
	 */
	public FireBrigadeInformation(int time, EntityID fbID, int hp, int damage,
			int buriedness, int water, EntityID areaID) {
		super(BaseMessageType.FIRE_BRIGADE, time);
		this.setData(new EntityIDData(DataType.FIRE_BRIGADE, fbID));
		this.setData(new ValueData(DataType.HP, hp));
		this.setData(new ValueData(DataType.DAMAGE, damage));
		this.setData(new ValueData(DataType.BURIEDNESS, buriedness));
		this.setData(new ValueData(DataType.WATER, water));
		this.setData(new EntityIDData(DataType.AREA, areaID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public FireBrigadeInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.FIRE_BRIGADE, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the fb.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getFireBrigadeID() {
		return super.getID(DataType.FIRE_BRIGADE, 0);
	}

	/**
	 * Return hp of the fb.
	 * 
	 * @return 
	 *         hp of the fb
	 */
	public int getHP() {
		return super.getHP(0);
	}

	/**
	 * Return damage of the fb.
	 * 
	 * @return 
	 *         damage
	 */
	public int getDamage() {
		return super.getDamage(0);
	}

	/**
	 * Return buriedness of the fb.
	 * 
	 * @return 
	 *         buriedness
	 */
	public int getBuriedness() {
		return super.getBuriedness(0);
	}

	/**
	 * Return amount of left water.
	 * 
	 * @return 
	 *         quantity.
	 */
	public int getWater() {
		return super.getWater(0);
	}

	/**
	 * Return EntityID of area that the fb is standing.
	 * 
	 * @return 
	 *         EntityID of area
	 */
	public EntityID getPositionID() {
		return super.getID(DataType.AREA, 0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getFireBrigadeID();
	}
}
