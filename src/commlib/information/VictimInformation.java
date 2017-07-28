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
 * The class show the information of victim.
 * 
 * @author takefumi
 * 
 */
public class VictimInformation extends WorldInformation {
	/**
	 * <h2>Constructor</h2> Create the victim information. Included data are
	 * follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>vicID:EntityID of the victim</li>
	 * <li>area:EntityID of area that the victim exist</li>
	 * <li>hp:hp of the victim</li>
	 * <li>buriedness:buriedness of the victim</li>
	 * <li>damage:damage of the victim</li>
	 * </ul>
	 * Additionally, This message can send the location data of the victim(
	 * {@literal Pair<Integer,Integer>} ), if location information doesn't
	 * exist, we get this as (-1,-1).<br>
	 * And not only EntityID of the civilian, but also EntityID of the platoon
	 * is assignable as vicID.
	 * 
	 * @param time
	 *            step num
	 * @param vicID
	 *            EntityID of the victim
	 * @param area
	 *            EntityID of area that the victim exist
	 * @param hp
	 *            hp of the victim
	 * @param buriedness
	 *            buriedness of the victim
	 * @param damage
	 *            damage of the victim
	 */
	public VictimInformation(int time, EntityID vicID, EntityID area, int hp,
			int buriedness, int damage) {
		super(BaseMessageType.VICTIM, time);
		// this.setData(new ValueData(DataType.HUMAN, vicID.getValue()));
		this.setData(new EntityIDData(DataType.HUMAN, vicID));
		// this.setData(new ValueData(DataType.AREA, area.getValue()));
		this.setData(new EntityIDData(DataType.AREA, area));
		this.setData(new ValueData(DataType.HP, hp));
		this.setData(new ValueData(DataType.BURIEDNESS, buriedness));
		this.setData(new ValueData(DataType.DAMAGE, damage));
	}

	/**
	 * <h2>Constructor</h2> Create the victim information. Included data are
	 * follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>vicID:EntityID of the victim</li>
	 * <li>area:EntityID of area that the victim exist</li>
	 * <li>hp:hp of the victim</li>
	 * <li>buriedness:buriedness of the victim</li>
	 * <li>damage:damage of the victim</li>
	 * <li>cor:location coordinate of the victim({@literal Pair<Integer,Integer>})
	 * </ul>
	 * Additionally, This message can send the location data of the victim(
	 * {@literal Pair<Integer,Integer>} ), if location information doesn't
	 * exist, we get this as (-1,-1).<br>
	 * And not only EntityID of the civilian, but also EntityID of the platoon
	 * is assignable as vicID.
	 * 
	 * @param time
	 *            step num
	 * @param vicID
	 *            EntityID of the victim
	 * @param area
	 *            EntityID of area that the victim exist
	 * @param hp
	 *            hp of the victim
	 * @param buriedness
	 *            buriedness of the victim
	 * @param damage
	 *            damage of the victim
	 * @param cor
	 *            coordinate of the victim
	 */
	public VictimInformation(int time, EntityID vicID, EntityID area, int hp,
			int buriedness, int damage, Pair<Integer, Integer> cor) {
		// this(time, vicID, area, hp, buriedness, damage);
		super(BaseMessageType.VICTIM_WITH_COORDINATE, time);
		// this.setData(new ValueData(DataType.HUMAN, vicID.getValue()));
		this.setData(new EntityIDData(DataType.HUMAN, vicID));
		// this.setData(new ValueData(DataType.AREA, area.getValue()));
		this.setData(new EntityIDData(DataType.HUMAN, area));
		this.setData(new ValueData(DataType.HP, hp));
		this.setData(new ValueData(DataType.BURIEDNESS, buriedness));
		this.setData(new ValueData(DataType.DAMAGE, damage));
		super.setCoorinate(cor);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public VictimInformation(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the victim.
	 * 
	 * @return EntityID
	 */
	public EntityID getVictimID() {
		return super.getID(DataType.HUMAN, 0);
	}

	/**
	 * Return hp of the victim
	 * 
	 * @return hp of the victim
	 */
	public int getHP() {
		return super.getHP(0);
	}

	/**
	 * Return buriedness of the victim
	 * 
	 * @return buriedness
	 */
	public int getBuriedness() {
		return super.getBuriedness(0);
	}

	/**
	 * Return damage of the victim
	 * 
	 * @return damage of the victim
	 */
	public int getDamage() {
		return super.getDamage(0);
	}

	/**
	 * Return EntityID of area that the victim exist
	 * 
	 * @return EntityID of area
	 */
	public EntityID getAreaID() {
		return super.getID(DataType.AREA, 0);
	}

	/**
	 * Return coordinate of the victim({@literal Pair<Integer,Integer>})<br>
	 * If not be setted, returned (-1,-1).
	 * 
	 * @return coordinate of thSe victim
	 */
	public Pair<Integer, Integer> getCoodinate() {
		return super.getCoodinate(0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getVictimID();
	}
}
