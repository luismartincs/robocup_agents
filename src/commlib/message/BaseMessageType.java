package commlib.message;

import commlib.data.DataType;

import javax.xml.crypto.Data;

/**
 * The BaseMessageType represents the basic message types that are provided.
 * <p>
 * There are three kinds of definitions.
 * <dl>
 * <dt>Information Message Type</dt>
 * <dd>This type indicates that the information obtained from the disaster
 * space.</dd>
 * <dd>BUILDING,BLOCKADE,BLOCKADE_WITH_COORDINATE,VICTIM,VICTIM_WITH_COORDINATE,
 * POSITION,TRANSFER_PATHWAY,FIRE_BRIGADE,POLICE_FORCE,AMBULANCE_TEAM and
 * UNPASSABLE correspond to this.</dd>
 * <dt>Task Message Type</dt>
 * <dd>This type indicates that the message is a task ordered agent has to
 * execute it.</dd>
 * <dd>CLEAR_ROUTE,RESCUE_AREA and EXTINGUISH_AREA correspond to this.</dd>
 * <dd>
 * REST_TASK, MOVE_TASK, CLEAR_BLOCKADE_TASK, RESCUE_TASK, EXTINGUISH_TASK and
 * WATER_SUPPLY_TASK correspond to this.</dd>
 * <dt>Report Message Type</dt>
 * <dd>This type indicates that the message is a report for the given task.</dd>
 * <dd>DONE and EXCEPTION correspond to this.</dd>
 * </dl>
 * 
 * @author takefumi
 * 
 */
public enum BaseMessageType {


	ACL_MESSAGE(new DataType[]{DataType.CENTER_AGENT,DataType.PERFORMATIVE}),

	UNPASSABLE(new DataType[] { DataType.PLATOON_AGENT, DataType.AREA,
			DataType.AREA, DataType.BLOCKADE }),

	// Information message type
	/**
	 * building
	 */
	BUILDING(new DataType[] { DataType.BUILDING, DataType.BROKENNESS,
			DataType.FIERYNESS }),
	/**
	 * blockade
	 */
	BLOCKADE(
			new DataType[] { DataType.BLOCKADE, DataType.ROAD,
					DataType.REPAIR_COST, DataType.X_COORDINATE,
					DataType.Y_COORDINATE }),
	/**
	 * blockade with coordinate
	 */
	BLOCKADE_WITH_COORDINATE(
			new DataType[] { DataType.BLOCKADE, DataType.ROAD,
					DataType.REPAIR_COST, DataType.X_COORDINATE,
					DataType.Y_COORDINATE }),
	/**
	 * victim
	 */
	VICTIM(new DataType[] { DataType.HUMAN, DataType.AREA, DataType.HP,
			DataType.BURIEDNESS, DataType.DAMAGE }),
	/**
	 * victime with coordinate
	 */
	VICTIM_WITH_COORDINATE(new DataType[] { DataType.HUMAN, DataType.AREA,
			DataType.HP, DataType.BURIEDNESS, DataType.DAMAGE,
			DataType.X_COORDINATE, DataType.Y_COORDINATE }),
	/**
	 * position
	 */
	POSITION(new DataType[] { DataType.PLATOON_AGENT, DataType.X_COORDINATE,
			DataType.Y_COORDINATE }),
	/**
	 * transfer pathway
	 */
	TRANSFER_PATHWAY(new DataType[] { DataType.PLATOON_AGENT,
			DataType.AREA_LIST }),
	/**
	 * fire brigade
	 */
	FIRE_BRIGADE(
			new DataType[] { DataType.FIRE_BRIGADE, DataType.HP,
					DataType.DAMAGE, DataType.BURIEDNESS, DataType.WATER,
					DataType.AREA }),
	/**
	 * police force
	 */
	POLICE_FORCE(new DataType[] { DataType.POLICE_FORCE, DataType.HP,
			DataType.DAMAGE, DataType.BURIEDNESS, DataType.AREA }),
	/**
	 * ambulance team
	 */
	AMBULANCE_TEAM(new DataType[] { DataType.AMBULANCE_TEAM, DataType.HP,
			DataType.DAMAGE, DataType.BURIEDNESS, DataType.AREA }),

	// Request message type
	// /**
	// * clear blockade request
	// */
	// CLEAR_BLOCKADE_REQUEST(new DataType[] { DataType.BLOCKADE,
	// DataType.REPAIR_COST }),
	// /**
	// * extinguish request
	// */
	// EXTINGUISH_REQUEST(new DataType[] { DataType.BUILDING }),
	// /**
	// * rescue request
	// */
	// RESCUE_REQUEST(new DataType[] { DataType.HUMAN }),

	// Task message type
	// common task
	/**
	 * rest task
	 */
	REST_TASK(new DataType[] { DataType.RESCUE_AGENT, DataType.PLATOON_AGENT }),
	/**
			 * 
			 */
	REST_AT_REFUGE_TASK(new DataType[] { DataType.RESCUE_AGENT,
			DataType.PLATOON_AGENT, DataType.REFUGE }),
	/**
	 * move task
	 */
	MOVE_TASK(new DataType[] { DataType.RESCUE_AGENT, DataType.PLATOON_AGENT,
			DataType.AREA }),
	/**
	 * move with staging post task
	 */
	MOVE_WITH_STAGING_POST_TASK(new DataType[] { DataType.RESCUE_AGENT,
			DataType.PLATOON_AGENT, DataType.AREA, DataType.AREA_LIST }),

	// Task Message
	// Police Force Task
	/**
	 * clear route(ex. ensure a route from A to B )
	 */
	CLEAR_ROUTE(new DataType[] { DataType.RESCUE_AGENT, DataType.POLICE_FORCE,
			DataType.AREA, DataType.AREA }),
	// Ambulance Team Task
	/**
	 * rescue area task(area : collection of entity.Area)
	 */
	RESCUE_AREA(new DataType[] { DataType.RESCUE_AGENT,
			DataType.AMBULANCE_TEAM, DataType.AREA_LIST }),
	// Fire Brigade Task
	/**
	 * extinguish area task
	 */
	EXTINGUISH_AREA(new DataType[] { DataType.RESCUE_AGENT,
			DataType.FIRE_BRIGADE, DataType.AREA_LIST }),
	/**
	 * scout area task
	 */
	SCOUT_AREA(new DataType[] { DataType.RESCUE_AGENT,
				DataType.PLATOON_AGENT, DataType.AREA_LIST }),	
	/**
	 * decide learer task
	 */
	DECIDE_LEADER(new DataType[] { DataType.RESCUE_AGENT,
				DataType.PLATOON_AGENT }),	
				
	// Report Message
	/**
	 * report message that task was done
	 */
	DONE(new DataType[] { DataType.PLATOON_AGENT }),
	/**
	 * report message that task cannot be execute
	 */
	EXCEPTION(new DataType[] { DataType.PLATOON_AGENT });

	DataType[] data;

	private BaseMessageType(DataType[] data) {
		this.data = data;
	}

	public DataType[] getDataType() {
		return this.data;
	}

	public int getTypeLength() {
		return this.data.length;
	}
}
