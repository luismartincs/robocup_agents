package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.ValueData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * This class represent the information of Ambulance Team.
 * 
 * @author takefumi
 * 
 */
public class AmbulanceTeamInformation extends WorldInformation {
	/**
	 * <h2>Constructor</h2> Create the Ambulance Team Information.<br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>atID:EntityID of Ambulance Team Agent.</li>
	 * <li>hp:HP of the at.</li>
	 * <li>damage: Damage of the at.</li>
	 * <li>buriedness:buriedness of at.</li>
	 * <li>areaID:EntityID of area that the at is standing.</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num.
	 * @param atID
	 *            EntityID of at.
	 * @param hp
	 *            hp of the at.
	 * @param damage
	 *            damage of the at.
	 * @param buriedness
	 *            buriedness of the at.
	 * @param areaID
	 *            EntityID of area that the at is standing.
	 */
	public AmbulanceTeamInformation(int time, EntityID atID, int hp,
			int damage, int buriedness, EntityID areaID) {
		super(BaseMessageType.AMBULANCE_TEAM, time);
		this.setData(new EntityIDData(DataType.AMBULANCE_TEAM, atID));
		this.setData(new ValueData(DataType.HP, hp));
		this.setData(new ValueData(DataType.DAMAGE, damage));
		this.setData(new ValueData(DataType.BURIEDNESS, buriedness));
		this.setData(new EntityIDData(DataType.AREA, areaID));
	}

	/**
	 * <h2>Constructor</h2> Create the instance of this class from bit sequence.<br>
	 * This method is defined for this library.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public AmbulanceTeamInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.AMBULANCE_TEAM, bitList, offset, bitSizeMap);
	}

	/**
	 * Return EntityID of at.
	 * 
	 * @return 
	 *         EntityID of at.
	 */
	public EntityID getAmbulanceTeamID() {
		return super.getID(DataType.AMBULANCE_TEAM, 0);
	}

	/**
	 * Return hp of the at.
	 * 
	 * @return 
	 *         hp
	 */
	public int getHP() {
		return super.getHP(0);
	}

	/**
	 * Return damage of the at.
	 * 
	 * @return 
	 *         damage
	 */
	public int getDamage() {
		return super.getDamage(0);
	}

	/**
	 * Return buriedness of the at.
	 * 
	 * @return 
	 *         buriedness
	 */
	public int getBuriedness() {
		return super.getBuriedness(0);
	}

	/**
	 * Return EntityID of area that the at is standing.
	 * 
	 * @return 
	 *         EntityID of area
	 */
	public EntityID getPositionID() {
		return super.getID(DataType.AREA, 0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getAmbulanceTeamID();
	}
}
