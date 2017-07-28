package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.ValueData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The class represent the police force information
 * 
 * @author takefumi
 * 
 */
public class PoliceForceInformation extends WorldInformation {

	/**
	 * <h2>Constructor</h2> Create the information of the police force.<br>
	 * Included data are follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>pfID:EntityID of the pf</li>
	 * <li>hp:hp of the pf</li>
	 * <li>damage:damage of the pf</li>
	 * <li>buriedness:buriedness of the pf</li>
	 * <li>areaID:EntityID of area that the pf is standing.</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param pfID
	 *            EntityID of the pf
	 * @param hp
	 *            hp of the pf
	 * @param damage
	 *            damage of the pf
	 * @param buriedness
	 *            buriedness of the pf
	 * @param areaID
	 *            EntityID of area
	 */
	public PoliceForceInformation(int time, EntityID pfID, int hp, int damage,
			int buriedness, EntityID areaID) {
		super(BaseMessageType.POLICE_FORCE, time);
		this.setData(new EntityIDData(DataType.POLICE_FORCE, pfID));
		this.setData(new ValueData(DataType.HP, hp));
		this.setData(new ValueData(DataType.DAMAGE, damage));
		this.setData(new ValueData(DataType.BURIEDNESS, buriedness));
		this.setData(new EntityIDData(DataType.AREA, areaID));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public PoliceForceInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.POLICE_FORCE, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of the pf.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getPoliceForceID() {
		return super.getID(DataType.POLICE_FORCE, 0);
	}

	/**
	 * Return hp of the pf.
	 * 
	 * @rketurn 
	 *          hp
	 */
	public int getHP() {
		return super.getHP(0);
	}

	/**
	 * Return damege of the pf.
	 * 
	 * @return 
	 *         damage
	 */
	public int getDamage() {
		return super.getDamage(0);
	}

	/**
	 * Return buryedness of the pf.
	 * 
	 * @return 
	 *         buryedness
	 */
	public int getBuriedness() {
		return super.getBuriedness(0);
	}

	/**
	 * Return EntityID of area that the pf is standing.
	 * 
	 * @return 
	 *         EntityID
	 */
	public EntityID getPositionID() {
		return super.getID(DataType.AREA, 0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getPoliceForceID();
	}

}
