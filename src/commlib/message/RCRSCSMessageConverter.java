package commlib.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import commlib.bdi.messages.ACLMessage;
import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.EntityIDListData;
import commlib.data.RCRSCSData;
import commlib.data.ValueData;
import commlib.information.AmbulanceTeamInformation;
import commlib.information.BlockadeInformation;
import commlib.information.BuildingInformation;
import commlib.information.FireBrigadeInformation;
import commlib.information.PoliceForceInformation;
import commlib.information.PositionInformation;
import commlib.information.TransferInformation;
import commlib.information.UnpassableInformation;
import commlib.information.VictimInformation;
import commlib.report.DoneReportMessage;
import commlib.report.ExceptionReportMessage;
import commlib.task.DecideLeaderTaskMessage;
import commlib.task.MoveTaskMessage;
import commlib.task.MoveWithStagingPostTaskMessage;
import commlib.task.RestAtRefugeTaskMessage;
import commlib.task.RestTaskMessage;
import commlib.task.ScoutAreaTaskMessage;
import commlib.task.TaskMessage;
import commlib.task.at.AmbulanceTeamTaskMessage;
import commlib.task.at.RescueAreaTaskMessage;
import commlib.task.fb.ExtinguishAreaTaskMessage;
import commlib.task.fb.FireBrigadeTaskMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import commlib.task.pf.PoliceForceTaskMessage;

import rescuecore2.config.Config;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 * The RCRSCSMessageConverter convert messages to bytes, and bytes to messages.
 * 
 * @author takefumi
 * 
 */
public class RCRSCSMessageConverter {
	private List<EntityID> buildingList;
	private List<EntityID> roadList;
	private List<EntityID> refugeList;
	private List<EntityID> areaList;

	private List<EntityID> policeForceList;
	private List<EntityID> ambulanceTeamList;
	private List<EntityID> fireBrigadeList;
	private List<EntityID> platoonAgentList;

	private List<EntityID> policeOfficeList;
	private List<EntityID> fireStationList;
	private List<EntityID> ambulanceCenterList;

	private List<EntityID> centerList;
	private List<EntityID> rescueList;

	private EntityID ownerID;
	private DataType agentType;

	private final int messageKind;
	private EnumMap<DataType, Integer> dataBitSizeMap;
	private EnumMap<BaseMessageType, Integer> messageMininumSizeMap;

	public final int EXTINGUISHABLE_POWER;
	public final int EXTINGUISHABLE_DISTANCE;
	public final int TANK_MAX;

	public final int HP_PERCEPTION_LOS;
	public final int DAMAGE_PERCEPTION_LOS;
	public final int RANGE_OF_VIEW;

	private static final boolean debug = false;

	@Deprecated
	public RCRSCSMessageConverter(EntityID ownerID, Config config,
			List<EntityID> buildingList, List<EntityID> roadList,
			List<EntityID> refugeList, List<EntityID> areaList,
			List<EntityID> policeForceList, List<EntityID> ambulanceTeamList,
			List<EntityID> fireBrigadeList, List<EntityID> platoonAgentList,
			List<EntityID> policeOfficeList,
			List<EntityID> ambulanceCenterList, List<EntityID> fireStationList) {
		super();
		this.ownerID = ownerID;
		this.buildingList = buildingList;
		this.roadList = roadList;
		this.refugeList = refugeList;
		this.areaList = areaList;
		this.policeForceList = policeForceList;
		this.ambulanceTeamList = ambulanceTeamList;
		this.fireBrigadeList = fireBrigadeList;
		this.platoonAgentList = platoonAgentList;

		this.policeOfficeList = policeOfficeList;
		this.ambulanceCenterList = ambulanceCenterList;
		this.fireStationList = fireStationList;
		this.centerList = new ArrayList<EntityID>();
		this.centerList.addAll(policeOfficeList);
		this.centerList.addAll(ambulanceCenterList);
		this.centerList.addAll(fireBrigadeList);
		this.rescueList = new ArrayList<EntityID>();
		this.rescueList.addAll(platoonAgentList);
		this.rescueList.addAll(centerList);
		EntityIDComparator comp = new EntityIDComparator();
		Collections.sort(this.centerList, comp);
		Collections.sort(this.rescueList, comp);
		this.checkAgentType();
		this.EXTINGUISHABLE_POWER = config.getIntValue(
				"fire.extinguish.max-sum", 500);
		this.EXTINGUISHABLE_DISTANCE = config.getIntValue(
				"fire.extinguish.max-distance", 50000);
		this.TANK_MAX = config.getIntValue("fire.tank.maximum", 7500);
		this.HP_PERCEPTION_LOS = config.getIntValue(
				"perception.los.precision.hp", 1000);
		this.DAMAGE_PERCEPTION_LOS = config.getIntValue(
				"perception.los.precision.damage", 100);
		this.RANGE_OF_VIEW = config.getIntValue("perception.los.max-distance",
				30000);

		this.messageKind = this
				.calculateBitSize(BaseMessageType.values().length);
		initBitSizeMap(this.dataBitSizeMap);
		initMessageMinimunSizeMap();
	}

	/**
	 * <h2>Constructor</h2> Prepare to convert.
	 * 
	 * @param ownerID
	 *            EntityID of this converter user(Agent or Center).
	 * @param model
	 * @param config
	 */
	public RCRSCSMessageConverter(EntityID ownerID, StandardWorldModel model,
			Config config) {
		this.ownerID = ownerID;
		this.checkAgentType(model.getEntity(ownerID));
		EntityIDComparator comp = new EntityIDComparator();
		this.buildingList = getIDList(model, comp, StandardEntityURN.BUILDING,
				StandardEntityURN.REFUGE, StandardEntityURN.AMBULANCE_CENTRE,
				StandardEntityURN.FIRE_STATION, StandardEntityURN.POLICE_OFFICE);
		this.roadList = getIDList(model, comp, StandardEntityURN.ROAD);
		this.refugeList = getIDList(model, comp, StandardEntityURN.REFUGE);
		this.areaList = getIDList(model, comp, StandardEntityURN.ROAD,
				StandardEntityURN.BUILDING, StandardEntityURN.AMBULANCE_CENTRE,
				StandardEntityURN.FIRE_STATION,
				StandardEntityURN.POLICE_OFFICE, StandardEntityURN.REFUGE);
		this.policeForceList = getIDList(model, comp,
				StandardEntityURN.POLICE_FORCE);
		this.ambulanceTeamList = getIDList(model, comp,
				StandardEntityURN.AMBULANCE_TEAM);
		this.fireBrigadeList = getIDList(model, comp,
				StandardEntityURN.FIRE_BRIGADE);
		this.platoonAgentList = getIDList(model, comp,
				StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM,
				StandardEntityURN.FIRE_BRIGADE);
		this.policeOfficeList = getIDList(model, comp,
				StandardEntityURN.POLICE_OFFICE);
		this.ambulanceCenterList = getIDList(model, comp,
				StandardEntityURN.AMBULANCE_CENTRE);
		this.fireStationList = getIDList(model, comp,
				StandardEntityURN.FIRE_STATION);
		this.centerList = getIDList(model, comp,
				StandardEntityURN.POLICE_OFFICE,
				StandardEntityURN.AMBULANCE_CENTRE,
				StandardEntityURN.FIRE_STATION);
		this.rescueList = getIDList(model, comp,
				StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM,
				StandardEntityURN.FIRE_BRIGADE,
				StandardEntityURN.POLICE_OFFICE,
				StandardEntityURN.AMBULANCE_CENTRE,
				StandardEntityURN.FIRE_STATION);

		this.EXTINGUISHABLE_POWER = config.getIntValue(
				"fire.extinguish.max-sum", 500);
		this.EXTINGUISHABLE_DISTANCE = config.getIntValue(
				"fire.extinguish.max-distance", 50000);
		this.TANK_MAX = config.getIntValue("fire.tank.maximum", 7500);
		this.HP_PERCEPTION_LOS = config.getIntValue(
				"perception.los.precision.hp", 1000);
		this.DAMAGE_PERCEPTION_LOS = config.getIntValue(
				"perception.los.precision.damage", 100);
		this.RANGE_OF_VIEW = config.getIntValue("perception.los.max-distance",
				30000);

		this.messageKind = calculateBitSize(BaseMessageType.values().length);
		initBitSizeMap(this.dataBitSizeMap);
		initMessageMinimunSizeMap();

		if (debug) {
			for (DataType type : this.dataBitSizeMap.keySet()) {
				System.out.println(type + " :" + this.dataBitSizeMap.get(type));
			}
			System.out.println("refuge size :"
					+ model.getEntitiesOfType(StandardEntityURN.REFUGE).size());
		}
	}

	/**
	 * Check type of agent that using this converter.<br>
	 * (at, fb, pf, ac, fs, po)
	 */
	private void checkAgentType() {
		this.agentType = DataType.HUMAN;
		if (this.ambulanceTeamList.contains(this.ownerID)) {
			this.agentType = DataType.AMBULANCE_TEAM;
		} else if (this.fireBrigadeList.contains(this.ownerID)) {
			this.agentType = DataType.FIRE_BRIGADE;
		} else if (this.policeForceList.contains(this.ownerID)) {
			this.agentType = DataType.POLICE_FORCE;
		} else if (this.ambulanceCenterList.contains(this.ownerID)) {
			this.agentType = DataType.AMBULANCE_CENTER;
		} else if (this.fireStationList.contains(this.ownerID)) {
			this.agentType = DataType.FIRE_STATION;
		} else if (this.policeOfficeList.contains(this.ownerID)) {
			this.agentType = DataType.POLICE_OFFICE;
		}
	}

	/**
	 * Check type of agent that using the converter.<br>
	 * (at, fb, pf, ac, fs, po)
	 * 
	 * @param se
	 *            agent that using the converter.
	 */
	private void checkAgentType(StandardEntity se) {
		this.agentType = DataType.HUMAN;
		if (se instanceof AmbulanceTeam) {
			this.agentType = DataType.AMBULANCE_TEAM;
		} else if (se instanceof FireBrigade) {
			this.agentType = DataType.FIRE_BRIGADE;
		} else if (se instanceof PoliceForce) {
			this.agentType = DataType.POLICE_FORCE;
		} else if (se instanceof AmbulanceCentre) {
			this.agentType = DataType.AMBULANCE_CENTER;
		} else if (se instanceof FireStation) {
			this.agentType = DataType.FIRE_STATION;
		} else if (se instanceof PoliceOffice) {
			this.agentType = DataType.POLICE_OFFICE;
		}
	}

	/**
	 * Return type of agent that have given EntityID.
	 */
	private DataType getAgentType(EntityID id) {
		DataType res = null;
		if (this.ambulanceTeamList.contains(id)) {
			res = DataType.AMBULANCE_TEAM;
		} else if (this.fireBrigadeList.contains(id)) {
			res = DataType.FIRE_BRIGADE;
		} else if (this.policeForceList.contains(id)) {
			res = DataType.POLICE_FORCE;
		} else if (this.ambulanceCenterList.contains(id)) {
			res = DataType.AMBULANCE_CENTER;
		} else if (this.fireStationList.contains(id)) {
			res = DataType.FIRE_STATION;
		} else if (this.policeOfficeList.contains(id)) {
			res = DataType.POLICE_OFFICE;
		}
		return res;
	}

	/**
	 * Return whether the agent that has given EntityID is center.
	 * 
	 * @param id
	 * @return if center : true<br>
	 *         otherwise : false
	 */
	private boolean isCenter(EntityID id) {
		return this.centerList.contains(id);
	}

	/**
	 * Return whether the agent that has given EntityID is rescue agent(at, fb,
	 * po).
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isPlatoon(EntityID id) {
		return this.platoonAgentList.contains(id);
	}

	/**
	 * Confirm whether two type of agents have the hierarchical relation.<br>
	 * e.g.)AC-AT, FS-FB, PO-PF
	 * 
	 * @param center
	 * @param agent
	 * @return
	 */
	private boolean isBelong(DataType center, DataType agent) {
		boolean res = false;
		if (agent.equals(DataType.AMBULANCE_TEAM)
				&& center.equals(DataType.AMBULANCE_CENTER)) {
			res = true;
		} else if (agent.equals(DataType.FIRE_BRIGADE)
				&& center.equals(DataType.FIRE_STATION)) {
			res = true;
		} else if (agent.equals(DataType.POLICE_FORCE)
				&& center.equals(DataType.POLICE_OFFICE)) {
			res = true;
		}
		return res;
	}

	/**
	 * Calculate bit num of each message that is minimum configuration.
	 */
	private void initMessageMinimunSizeMap() {
		this.messageMininumSizeMap = new EnumMap<BaseMessageType, Integer>(
				BaseMessageType.class);
		for (BaseMessageType type : BaseMessageType.values()) {
			this.messageMininumSizeMap.put(type, messageMinimumBitSize(type));
		}
	}

	/**
	 * Calculate bit num of a message that is minimum configuration.
	 * 
	 * @param m
	 *            message type
	 * @return bit num
	 */
	private int messageMinimumBitSize(BaseMessageType m) {
		int res = 0;
		for (DataType dt : RCRSCSMessage.COMMON_DATA_TYPE) {
			res += this.dataBitSizeMap.get(dt);
		}
		for (DataType dt : m.getDataType()) {
			if (dt == DataType.ID_LIST || dt == DataType.AREA_LIST) {
				res += 32;
			} else {
				res += this.dataBitSizeMap.get(dt);
			}
		}
		return res;
	}

	/**
	 * Calculate bit num necessary for sending each data.
	 * 
	 * @param map
	 */
	private void initBitSizeMap(EnumMap<DataType, Integer> map) {


		this.dataBitSizeMap = new EnumMap<DataType, Integer>(DataType.class);

		/**
		 * @CinvesRudos
		 *
		 */
		//performative

		this.dataBitSizeMap.put(DataType.PERFORMATIVE,32);

		// time
		this.dataBitSizeMap.put(DataType.TIME, calculateBitSize(1000));
		// coordinate
		this.dataBitSizeMap.put(DataType.X_COORDINATE, 32);
		this.dataBitSizeMap.put(DataType.Y_COORDINATE, 32);
		// objects
		this.dataBitSizeMap.put(DataType.AREA,
				calculateBitSize(this.areaList.size()));
		this.dataBitSizeMap.put(DataType.ROAD,
				calculateBitSize(this.roadList.size()));
		this.dataBitSizeMap.put(DataType.BUILDING,
				calculateBitSize(this.buildingList.size()));
		this.dataBitSizeMap.put(DataType.REFUGE,
				calculateBitSize(this.refugeList.size()));
		this.dataBitSizeMap.put(DataType.BLOCKADE, 32);
		// agents
		this.dataBitSizeMap.put(DataType.HUMAN, 32);
		this.dataBitSizeMap.put(DataType.FIRE_BRIGADE,
				calculateBitSize(this.fireBrigadeList.size()));
		this.dataBitSizeMap.put(DataType.AMBULANCE_TEAM,
				calculateBitSize(this.ambulanceTeamList.size()));
		this.dataBitSizeMap.put(DataType.POLICE_FORCE,
				calculateBitSize(this.policeForceList.size()));
		this.dataBitSizeMap.put(DataType.PLATOON_AGENT,
				calculateBitSize(this.platoonAgentList.size()));
		this.dataBitSizeMap.put(DataType.CENTER_AGENT,
				calculateBitSize(this.centerList.size()));
		this.dataBitSizeMap.put(DataType.RESCUE_AGENT,
				calculateBitSize(this.rescueList.size()));
		// value
		this.dataBitSizeMap.put(DataType.HP,
				calculateBitSize((10000 / this.HP_PERCEPTION_LOS) + 1));
		this.dataBitSizeMap.put(DataType.DAMAGE,
				calculateBitSize((10000 / this.DAMAGE_PERCEPTION_LOS) + 1));
		this.dataBitSizeMap.put(DataType.BURIEDNESS, calculateBitSize(200));
		this.dataBitSizeMap.put(DataType.BROKENNESS, calculateBitSize(200));
		this.dataBitSizeMap.put(DataType.FIERYNESS,
				calculateBitSize(Fieryness.values().length));
		this.dataBitSizeMap.put(DataType.REPAIR_COST, calculateBitSize(1000));
		// this.dataBitSizeMap.put(DataType.SUPPLY_QUANTITY,
		// calculateBitSize(this.TANK_MAX + 1));
		this.dataBitSizeMap.put(DataType.WATER_POWER,
				calculateBitSize(this.EXTINGUISHABLE_POWER));
		this.dataBitSizeMap.put(DataType.WATER, calculateBitSize(7500));
	}

	/**
	 * Return List of EntityID of Entity that have given StandardEntityURN.
	 * 
	 * @param model
	 * @param comp
	 * @param urn
	 * @return (sorted)List of EntityID
	 */
	private List<EntityID> getIDList(StandardWorldModel model,
			EntityIDComparator comp, StandardEntityURN urn) {
		return toIDList(model.getEntitiesOfType(urn), comp);
	}

	/**
	 * Return List of EntityID of Entity that have given StandardEntityURNs.
	 * 
	 * @param model
	 * @param comp
	 * @param urns
	 * @return (sorted)List of EntityID
	 */
	private List<EntityID> getIDList(StandardWorldModel model,
			EntityIDComparator comp, StandardEntityURN... urns) {
		return toIDList(model.getEntitiesOfType(urns), comp);
	}

	/**
	 * Return EntityID list created from the collection of StandardEntity.
	 * 
	 * @param col
	 * @param comp
	 * @return (sorted)List of EntityID
	 */
	private List<EntityID> toIDList(Collection<StandardEntity> col,
			EntityIDComparator comp) {
		List<EntityID> res = new ArrayList<EntityID>();
		for (StandardEntity se : col) {
			res.add(se.getID());
		}
		Collections.sort(res, comp);
		return res;
	}

	/**
	 * Convert received bytes to message list.<br>
	 * In case failure happen halfway through converting, return list of message
	 * converted by then.
	 * 
	 * @param bytes
	 *            converting bytes
	 * @return message list
	 */
	public List<RCRSCSMessage> bytesToMessageList(byte[] bytes) {
		List<Integer> bitList = toBit(bytes);
		return bitToMessages(bitList);
	}

	/**
	 * Convert to bit sequence to message list.
	 * 
	 * @param bitList
	 * @return message list
	 */
	private List<RCRSCSMessage> bitToMessages(List<Integer> bitList) {
		List<RCRSCSMessage> res = new ArrayList<RCRSCSMessage>();
		List<TaskMessage> taskList = new ArrayList<TaskMessage>();
		if (debug) {
			System.out.println("-----------byte to message---------------");
		}
		for (int offset = 0; offset < bitList.size();) {
			if (debug) {
				System.out.println(res.size());
			}
			BaseMessageType tmp = null;
			try {
				if (bitList.size() <= offset + 7) {
					break;
				}
				// get message type
				if (bitList.size() - offset < this.messageKind) {
					break;
				}
				BaseMessageType mType = null;
				int mTypeValue = bitToInt(bitList, offset, this.messageKind);
				if (debug) {
					System.out.println("offset" + offset + ";" + mTypeValue);
				}
				offset += this.messageKind;
				mType = BaseMessageType.values()[mTypeValue];
				tmp = mType;
				if (bitList.size() < offset + this.messageMinimumBitSize(mType)) {
					break;
				}
				RCRSCSMessage message = null;
				switch (mType) {
				case BUILDING:
					message = new BuildingInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				case BLOCKADE:
				case BLOCKADE_WITH_COORDINATE:
					message = new BlockadeInformation(mType, bitList, offset,
							this.dataBitSizeMap);
					break;
				case VICTIM:
				case VICTIM_WITH_COORDINATE:
					message = new VictimInformation(mType, bitList, offset,
							this.dataBitSizeMap);
					break;
				case POSITION:
					message = new PositionInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				case TRANSFER_PATHWAY:
					message = new TransferInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				case FIRE_BRIGADE:
					message = new FireBrigadeInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				case POLICE_FORCE:
					message = new PoliceForceInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				case AMBULANCE_TEAM:
					message = new AmbulanceTeamInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				case UNPASSABLE:
					message = new UnpassableInformation(bitList, offset,
							this.dataBitSizeMap);
					break;
				// case CLEAR_BLOCKADE_REQUEST:
				// message = new ClearBlockadeRequest(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				// case EXTINGUISH_REQUEST:
				// message = new ExtinguishRequest(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				// case RESCUE_REQUEST:
				// message = new RescueRequest(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				case REST_TASK:
					message = new RestTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case REST_AT_REFUGE_TASK:
					message = new RestAtRefugeTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case MOVE_TASK:
					message = new MoveTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case MOVE_WITH_STAGING_POST_TASK:
					message = new MoveWithStagingPostTaskMessage(bitList,
							offset, this.dataBitSizeMap);
					break;
				// case CLEAR_BLOCKADE_TASK:
				// message = new ClearBlockadeTaskMessage(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				// case CLEAR_AREA_TASK:
				// message = new ClearAreaTaskMessage(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				// case RESCUE_TASK:
				// message = new RescueTaskMessage(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				// case EXTINGUISH_TASK:
				// message = new ExtinguishTaskMessage(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				// case WATER_SUPPLY_TASK:
				// message = new WaterSupplyTaskMessage(bitList, offset,
				// this.dataBitSizeMap);
				// break;
				case CLEAR_ROUTE:
					message = new ClearRouteTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case RESCUE_AREA:
					message = new RescueAreaTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case EXTINGUISH_AREA:
					message = new ExtinguishAreaTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case SCOUT_AREA:
					message = new ScoutAreaTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case DECIDE_LEADER:
					message = new DecideLeaderTaskMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case DONE:
					message = new DoneReportMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
				case EXCEPTION:
					message = new ExceptionReportMessage(bitList, offset,
							this.dataBitSizeMap);
					break;
                case ACL_MESSAGE:
                    message = new ACLMessage(bitList, offset, this.dataBitSizeMap);
						break;

				default:
					throw new Exception("undefined message type " + mType
							+ "\n" + "decode was stopped on the way...");
				}
				offset += message.getMessageBitSize(this.dataBitSizeMap);
				if (debug) {
					System.out.println("messagebyte "
							+ message.getMessageBitSize(this.dataBitSizeMap)
							+ " " + this.messageKind + " offset :" + offset);
				}
				EnumMap<DataType, Integer> counter = new EnumMap<DataType, Integer>(
						DataType.class);
				for (DataType dt : message.getDataTypeArray()) {
					int i = getDataTypeIndex(counter, dt, 1);
					RCRSCSData<?> messageData = message.getData(dt, i);
					convertToRealData(messageData);
				}
				if (message instanceof TaskMessage) {
					TaskMessage task = (TaskMessage) message;
//					if (task.getAssignedAgentID().equals(this.ownerID)) {
						taskList.add(task);
//					}
				} else {
					res.add(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err
						.println("This exception caused by message decoding step:"
								+ tmp);
				break;
			}
		}
		// count = 0;
		TaskMessage task = filteringTask(taskList);
		if (task != null) {
			res.add(task);
		}
		if (debug) {
			System.out.println("-----------------------------------------");
		}
		return res;
	}

	private int count = 0;

	/**
	 * Filter the given task subject to the following conditions.<br>
	 * 1.received task is sent from center.<br>
	 * 2.if step 1 is true, the center is a direct superior.<br>
	 * 3.if step 1 is false, the task is sent from same type of agent.<br>
	 * 4.if step 3 is false, return value is null at the time.<br>
	 * 
	 * @param taskList
	 * @return
	 */
	private TaskMessage filteringTask(List<TaskMessage> taskList) {
		TaskMessage res = null;
		DataType currentType = null;
		for (TaskMessage task : taskList) {
			EntityID moId = task.getMessageOwnerID();
			EntityID agId = task.getAssignedAgentID();
			DataType type = this.getAgentType(agId);
			if (type != null && canExecute(task, type)) {
				if (this.isCenter(moId)) {
					if (this.isBelong(type, this.agentType)) {
						res = task;
						break;
					} else if (res == null || currentType == null
							|| !currentType.equals(DataType.CENTER_AGENT)) {
						res = task;
						currentType = DataType.CENTER_AGENT;
					}
				} else if (this.agentType.equals(type) && currentType != null
						&& !this.agentType.equals(currentType)) {
					res = task;
					currentType = type;
				} else if (res == null) {
					res = task;
					currentType = type;
				}
			}
		}
		return res;
	}

	/**
	 * Check the transmitted task is executable for the agent received this
	 * task.
	 * 
	 * @param task
	 * @param agentType
	 * @return executable : true<br>
	 *         otherwise : false
	 */
	private boolean canExecute(TaskMessage task, DataType agentType) {
		boolean res = false;
		if (task instanceof AmbulanceTeamTaskMessage
				&& agentType.equals(DataType.AMBULANCE_TEAM)) {
			res = true;
		} else if (task instanceof PoliceForceTaskMessage
				&& agentType.equals(DataType.POLICE_FORCE)) {
			res = true;
		} else if (task instanceof FireBrigadeTaskMessage
				&& agentType.equals(DataType.FIRE_BRIGADE)) {
			res = true;
		} else if (task instanceof ScoutAreaTaskMessage){
			res = true;
		} else if (task instanceof DecideLeaderTaskMessage){
			res = true;
		}
		return true;
		/*return res;*/
	}

	/**
	 * fine-tune each data of message created by convert.
	 * 
	 * @param messageData
	 */
	private void convertToRealData(RCRSCSData<?> messageData) {
		if (messageData instanceof EntityIDListData) {
			List<EntityID> ids = new ArrayList<EntityID>();
			switch (messageData.getType()) {
			case AREA_LIST:
				for (EntityID id : ((EntityIDListData) messageData).getData()) {
					ids.add(this.areaList.get(id.getValue()));
				}
				((EntityIDListData) messageData).setData(ids);
				break;
			}
		} else {
			switch (messageData.getType()) {

			case AMBULANCE_TEAM:
				((EntityIDData) messageData).setData(this.ambulanceTeamList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case FIRE_BRIGADE:
				((EntityIDData) messageData).setData(this.fireBrigadeList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case POLICE_FORCE:
				((EntityIDData) messageData).setData(this.policeForceList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case PLATOON_AGENT:
				((EntityIDData) messageData).setData(this.platoonAgentList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case CENTER_AGENT:
				try{((EntityIDData) messageData).setData(this.centerList
						.get(((EntityID) messageData.getData()).getValue()));}catch (Exception ex){
					System.out.println("VALIO QUESO");
				}
				try {
					((EntityIDData) messageData).setData(this.platoonAgentList.get(((EntityID) messageData.getData()).getValue()));
				}catch (Exception ex){
					System.out.println("FALLO AL BUSCAR 2");
				}

				break;
			case RESCUE_AGENT:
				((EntityIDData) messageData).setData(this.rescueList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case AREA:
				((EntityIDData) messageData).setData(this.areaList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case ROAD:
				((EntityIDData) messageData).setData(this.roadList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case BUILDING:
				((EntityIDData) messageData).setData(this.buildingList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case REFUGE:
				((EntityIDData) messageData).setData(this.refugeList
						.get(((EntityID) messageData.getData()).getValue()));
				break;
			case HP:
				((ValueData) messageData).setData(((ValueData) messageData)
						.getData() * this.HP_PERCEPTION_LOS);
				break;
			case DAMAGE:
				((ValueData) messageData).setData(((ValueData) messageData)
						.getData() * this.DAMAGE_PERCEPTION_LOS);
				break;
			case BLOCKADE:
			case HUMAN:
			case TIME:
			case FIERYNESS:
			case BURIEDNESS:
			case BROKENNESS:
			case X_COORDINATE:
			case Y_COORDINATE:
			case WATER_POWER:
				// case SUPPLY_QUANTITY:
			case REPAIR_COST:
			case WATER:

			/**
			 * @CinvesRudos
			 */
			case PERFORMATIVE:
				break;
			default:
				System.err.println("undefined data type:"
						+ messageData.getType());
			}
		}
	}

	/**
	 * Create int from a part of bit sequence.
	 * 
	 * @param list
	 * @param index
	 * @param length
	 * @return int
	 */
	public static int bitToInt(List<Integer> list, int index, int length) {
		int res = 0;
		try {
			for (int i = index; i < index + length; i++) {
				res = (res << 1) | list.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(list.size() + " " + index + " " + length);
			System.exit(-1);
		}
		return res;
	}

	/**
	 * Change received bytes to bit sequence.
	 * 
	 * @param bytes
	 * @return
	 */
	private static List<Integer> toBit(byte[] bytes) {
		List<Integer> res = new ArrayList<Integer>();
		for (byte b : bytes) {
			res.addAll(toBit(b, 8));
		}
		return res;
	}

	/**
	 * Convert messages to bytes<br>
	 * The messages that failed to cenvert are not contained.
	 * 
	 * @param messages
	 *            List of messages
	 * @return transmitted byte sequence
	 */
	public byte[] messageToBytes(List<RCRSCSMessage> messages) {
		if (debug) {
			System.out.println(messages.size());
		}
		byte[] res = null;
		if (messages.size() > 0) {
			List<Integer> bitList = new ArrayList<Integer>();
			for (RCRSCSMessage m : messages) {
				List<Integer> bits = messageToBit(m);
				bitList.addAll(bits);
			}
			messages.clear();
			res = getBytes(bitList);
			if (debug) {
				System.out.println("bitList size :" + bitList.size());
			}
		}
		return res;
	}

	/**
	 * Convert bit list to bytes.<br>
	 * This is private method used on converting.
	 * 
	 * @param bitList
	 * @return
	 */
	private byte[] getBytes(List<Integer> bitList) {
		int index = 0;
		int offset = 7;
		byte[] res = new byte[(bitList.size() - 1) / 8 + 1];
		for (int bit : bitList) {
			res[index] |= bit << offset;
			offset = (offset + 7) % 8;
			if (offset == 7) {
				index++;
			}
		}
		return res;
	}

	/**
	 * Convert one message to bit list.<br>
	 * This is private method used on converting.
	 * 
	 * @param message
	 *            converted message
	 * @return bit list
	 */
	private List<Integer> messageToBit(RCRSCSMessage message) {
		List<Integer> res = new ArrayList<Integer>();
		EnumMap<DataType, Integer> counter = new EnumMap<DataType, Integer>(
				DataType.class);
		try {
			// add message type
			res.addAll(toBit(message.getMessageType().ordinal(),
					this.messageKind));
			// add message data
			for (DataType dt : message.getDataTypeArray()) {
				int index = getDataTypeIndex(counter, dt, 1);
				RCRSCSData<?> data = message.getData(dt, index);
				if (data instanceof EntityIDListData) {
					List<Integer> values = convertToMessageValue((EntityIDListData) data);
					int bitLength = 32;
					if (dt == DataType.AREA_LIST) {
						bitLength = this.dataBitSizeMap.get(DataType.AREA);
					}
					// add list size
					res.addAll(toBit(values.get(0), 32));
					for (int i = 1; i < values.size(); i++) {
						res.addAll(toBit(values.get(i), bitLength));
					}
				} else {
					if (debug) {
						System.out.println(data.getType());
					}
					res.addAll(toBit(convertToMessageValue(data),
							this.dataBitSizeMap.get(dt)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("This message will not be sended."
					+ message.getMessageType());
			res.clear();
		}
		return res;
	}

	/**
	 * Convert value to bit list that have specified length.<br>
	 * This is private method used on converting.
	 * 
	 * @param value
	 * @param length
	 * @return
	 */
	private static List<Integer> toBit(int value, int length) {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < length; i++) {
			res.add((value >> i) & 0x01);
		}
		Collections.reverse(res);
		return res;
	}

	/**
	 * Convert each EntityIDListData to bit list for transmission.<br>
	 * This is private method used on converting.
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private List<Integer> convertToMessageValue(EntityIDListData data)
			throws Exception {
		List<Integer> res = new ArrayList<Integer>();
		try {
			List<EntityID> ids = (List<EntityID>) data.getData();
			if (ids.size() >= 0) {
				res.add(ids.size());
				switch (data.getType()) {
				case ID_LIST:
					for (EntityID id : ids) {
						int val = id.getValue();
						res.add(val);
					}
					break;
				case AREA_LIST:
					for (EntityID id : ids) {
						int index = this.areaList.indexOf(id);
						res.add(index);
					}
					break;
				}
			}
		} catch (Exception e) {
			throw new Exception("EntityIDList needs at least 1 id...");
		}
		return res;
	}

	/**
	 * Convert each RCRSCSData(excepting EntityIDListData) to bit list for
	 * transmission.<br>
	 * This is private method used on converting.
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private int convertToMessageValue(RCRSCSData<?> data) throws Exception {
		int res = -1;
		try {
			switch (data.getType()) {
			case AMBULANCE_TEAM:
				res = this.ambulanceTeamList.indexOf((EntityID) data.getData());
				break;
			case FIRE_BRIGADE:
				res = this.fireBrigadeList.indexOf((EntityID) data.getData());
				break;
			case POLICE_FORCE:
				res = this.policeForceList.indexOf((EntityID) data.getData());
				break;
			case PLATOON_AGENT:
				res = this.platoonAgentList.indexOf((EntityID) data.getData());
				break;
			case CENTER_AGENT:
				res = this.centerList.indexOf((EntityID) data.getData());
				break;
			case RESCUE_AGENT:
				res = this.rescueList.indexOf((EntityID) data.getData());
				break;
			case AREA:
				res = this.areaList.indexOf((EntityID) data.getData());
				break;
			case ROAD:
				res = this.roadList.indexOf((EntityID) data.getData());
				break;
			case BUILDING:
				res = this.buildingList.indexOf((EntityID) data.getData());
				if (debug) {
					System.out.println("building index:" + res
							+ "   buildingID:" + (EntityID) data.getData());
				}
				break;
			case REFUGE:
				res = this.refugeList.indexOf((EntityID) data.getData());
				break;
			case HP:
				res = (Integer) data.getData() / this.HP_PERCEPTION_LOS;
				break;
			case DAMAGE:
				res = (Integer) data.getData() / this.DAMAGE_PERCEPTION_LOS;
				break;
			case BLOCKADE:
			case HUMAN:
				res = ((EntityID) data.getData()).getValue();
				break;
			case TIME:
			case FIERYNESS:
			case BURIEDNESS:
			case BROKENNESS:
			case X_COORDINATE:
			case Y_COORDINATE:
			case WATER_POWER:
				// case SUPPLY_QUANTITY:
			case REPAIR_COST:
			case WATER:
			/**
			 * @CinvesRudos
			 */
			case PERFORMATIVE:

				res = (Integer) data.getData();
				break;
			default:
				System.err.println("undefined data type:" + data.getType());
			}
		} catch (Exception e) {
			System.err.println(data.getType());
			// e.printStackTrace();
			throw e;
		}
		return res;
	}

	/**
	 * This is private method used on converting.
	 * 
	 * @param map
	 * @param dType
	 * @param i
	 * @return
	 */
	public static int getDataTypeIndex(EnumMap<DataType, Integer> map,
			DataType dType, Integer i) {
		Integer val = map.get(dType);
		if (val == null) {
			val = 0;
		} else {
			val += i;
		}
		map.put(dType, val);
		return val;
	}

	/**
	 * Calculate bit num necessary for express value.<br>
	 * This is private method used on converting.
	 * 
	 * @param value
	 * @return
	 */
	private int calculateBitSize(int value) {
		int res = 0;
		if (value == 1) {
			res = 1;
		} else if (value > 1) {
			res = (int) Math.ceil(Math.log10(value) / Math.log10(2.0d));
		}
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ownerID == null) ? 0 : ownerID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RCRSCSMessageConverter other = (RCRSCSMessageConverter) obj;
		if (ownerID == null) {
			if (other.ownerID != null)
				return false;
		} else if (!ownerID.equals(other.ownerID))
			return false;
		return true;
	}

}
