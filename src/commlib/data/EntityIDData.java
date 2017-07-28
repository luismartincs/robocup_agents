package commlib.data;

import rescuecore2.worldmodel.EntityID;

/**
 * This class represent the data of EntityID.<br>
 * 
 * @author takefumi
 * 
 */
public class EntityIDData extends RCRSCSData<EntityID> {

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type of the data({@link DataType})
	 * @param value
	 *            actual value of data(EntityID)
	 */
	public EntityIDData(DataType type, EntityID value) {
		super(type);
		this.value = value;
	}

	// public EntityIDData(DataType type) {
	// super(type);
	// this.value = null;
	// }

	@Override
	public void setData(EntityID obj) {
		this.value = obj;
	}
}
