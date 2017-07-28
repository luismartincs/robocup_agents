package commlib.information;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.EntityIDListData;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * This class represent the information of pathway of the rescue angets.
 * 
 * @author takefumi
 * 
 */
public class TransferInformation extends WorldInformation {
	/**
	 * <h2>Constructor</h2> Create the information of the agent's pathway.<br>
	 * Included data are follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>platoonID:EntityID of the rescue agent(pf,at,fb).</li>
	 * <li>areas:agent's pathway</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param platoonID
	 *            EntityID of the rescue agent
	 * @param areas
	 *            areas that the agent passed
	 */
	public TransferInformation(int time, EntityID platoonID, EntityID... areas) {
		super(BaseMessageType.TRANSFER_PATHWAY, time);
		// List<Integer> ids = new ArrayList<Integer>();
		// for (EntityID id : areas) {
		// ids.add(id.getValue());
		// }
		// this.setData(new ListData(DataType.AREA_LIST, ids));
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, platoonID));
		List<EntityID> areaList = new ArrayList<EntityID>();
		for (EntityID id : areas) {
			areaList.add(id);
		}
		this.setData(new EntityIDListData(DataType.AREA_LIST, areaList));
	}

	/**
	 * <h2>Constructor</h2> Create the information of the agent's pathway.<br>
	 * Included data are follow.
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>platoonID:EntityID of the rescue agent(pf,at,fb).</li>
	 * <li>areas:agent's pathway({@literal List<EntityID})</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param platoonID
	 *            EntityID of the rescue agent
	 * @param areas
	 *            List of the agent's pathway({@literal List<EntityID})
	 */
	public TransferInformation(int time, EntityID platoonID,
			List<EntityID> areas) {
		// this(time, areas.toArray(new EntityID[areas.size()]));
		super(BaseMessageType.TRANSFER_PATHWAY, time);
		this.setData(new EntityIDData(DataType.PLATOON_AGENT, platoonID));
		this.setData(new EntityIDListData(DataType.AREA_LIST, areas));
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public TransferInformation(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.TRANSFER_PATHWAY, bitList, offset, bitSizeMap);
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
	 * Return the rescue agent's pathway
	 * 
	 * @return 
	 *         EntityID list of areas
	 */
	public List<EntityID> getPathway() {
		return super.getEntityIDList(DataType.AREA_LIST, 0);
	}

	@Override
	public EntityID getEntityID() {
		return this.getAgentID();
	}

}
