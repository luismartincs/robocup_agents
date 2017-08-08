package commlib.data;

import rescuecore2.worldmodel.EntityID;

/**
 * This class show that what kind of value the data is.
 * 
 * @author takefumi
 * 
 */
public enum DataType {
	// value type

	/**
	 * @CinvesRudos
	 * Para los mensajes ACL
	 */

	ALL_ENTITIES,



	PERFORMATIVE,

	/**
	 * time that simulation is started.
	 */
	TIME,

	/**
	 * x coordinate of the entities.
	 */
	X_COORDINATE,
	/**
	 * y coordinate of the entities.
	 */
	Y_COORDINATE,
	/**
	 * area id.
	 */
	AREA,
	/**
	 * road id.
	 */
	ROAD,
	/**
	 * building id.
	 */
	BUILDING,
	/**
	 * refuge id.
	 */
	REFUGE,
	/**
	 * blockade id.
	 */
	BLOCKADE,
	/**
	 * id of human agent(pf, fb, at, civ)
	 */
	HUMAN,
	/**
	 * fb id.
	 */
	FIRE_BRIGADE,
	/**
	 * at id.
	 */
	AMBULANCE_TEAM,
	/**
	 * pf id.
	 */
	POLICE_FORCE,
	/**
	 * id of platoon agent(pf, fb, at)
	 */
	PLATOON_AGENT,
	/**
	 * fs id.
	 */
	FIRE_STATION,
	/**
	 * ac id.
	 */
	AMBULANCE_CENTER,
	/**
	 * po id.
	 */
	POLICE_OFFICE,
	/**
	 * id of center(fs, ac, po)
	 */
	CENTER_AGENT,
	/**
	 * id of rescuer(fb, at, pf, fs, ac, po)
	 */
	RESCUE_AGENT,
	/**
	 * HP of Agent.
	 */
	HP,
	/**
	 * Damage of Agent.
	 */
	DAMAGE,
	/**
	 * Agent buriedness.
	 */
	BURIEDNESS,
	/**
	 * Agent fieryness.
	 */
	FIERYNESS,
	/**
	 * discharge water power.
	 */
	WATER_POWER,
	// SUPPLY_QUANTITY,
	/**
	 * blockade repair cost.
	 */
	REPAIR_COST,
	/**
	 * building brokenness.
	 */
	BROKENNESS,
	/**
	 * water quantity that fb has.
	 */
	WATER,
	// list type
	/**
	 * list of entity id.
	 */
	@Deprecated
	ID_LIST,
	/**
	 * list of area id.
	 */
	AREA_LIST;

	/**
	 * Return {@link RCRSCSData} created from type and value.
	 * 
	 * @param type
	 *            DataType
	 * @param value
	 *            actual value(int)
	 * @return RCRSCSData
	 */
	public static RCRSCSData<?> createData(DataType type, int value) {
		RCRSCSData<?> res = null;
		switch (type) {
		case PERFORMATIVE:
		case TIME:
		case X_COORDINATE:
		case Y_COORDINATE:
		case HP:
		case DAMAGE:
		case BURIEDNESS:
		case FIERYNESS:
		case WATER_POWER:
			// case SUPPLY_QUANTITY:
		case REPAIR_COST:
		case BROKENNESS:
		case WATER:
			res = new ValueData(type, value);
			break;
		case ALL_ENTITIES:
		case AREA:
		case ROAD:
		case BUILDING:
		case REFUGE:
		case BLOCKADE:
		case HUMAN:
		case FIRE_BRIGADE:
		case AMBULANCE_TEAM:
		case POLICE_FORCE:
		case PLATOON_AGENT:
		case CENTER_AGENT:
		case RESCUE_AGENT:
			res = new EntityIDData(type, new EntityID(value));
		default:
		}
		return res;
	}

	/**
	 * Return {@link EntityIDListData} created from type.<br>
	 * Here, only ID_LIST and AREA_LIST is accepted.
	 * 
	 * @param type
	 *            {@link DataType}
	 * @return {@link EntityIDListData}
	 */
	public static EntityIDListData createIDListData(DataType type) {
		EntityIDListData res = null;
		switch (type) {
		case ID_LIST:
		case AREA_LIST:
			res = new EntityIDListData(type);
			break;
		default:
		}
		return res;
	}
}
