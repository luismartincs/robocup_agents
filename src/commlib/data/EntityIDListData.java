package commlib.data;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;

/**
 * The EntityIDListData represent the data that is the list of EntityID.
 * 
 * @author takefumi
 * 
 */
public class EntityIDListData extends RCRSCSData<List<EntityID>> {

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type of the data({@link DataType})
	 * @param value
	 *            actual value of the data({@literal List<EntityID>})
	 */
	public EntityIDListData(DataType type, List<EntityID> value) {
		super(type);
		this.value = new ArrayList<EntityID>(value);
	}

	/**
	 * Constructor<br>
	 * In this method, value is empty list.
	 * 
	 * @param type
	 */
	public EntityIDListData(DataType type) {
		super(type);
		this.value = new ArrayList<EntityID>();
	}

	@Override
	public void setData(List<EntityID> obj) {
		this.value = new ArrayList<EntityID>(obj);
	}

	/**
	 * Add obj(EntityID) to value.
	 * 
	 * @param obj
	 *            additional value(EntityID)
	 */
	public void setData(EntityID obj) {
		this.value.add(obj);
	}
}
