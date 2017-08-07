package commlib.information;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.message.BaseMessageType;
import commlib.message.RCRSCSMessage;

import rescuecore2.worldmodel.EntityID;

/**
 * The abstract class represent the information that can obtain from the
 * disaster space.
 * 
 * @author takefumi
 * 
 */
public abstract class WorldInformation extends RCRSCSMessage {
	/**
	 * <h2>Constructor</h2>
	 * 
	 * @param type
	 * @param time
	 */
	public WorldInformation(BaseMessageType type, int time) {
		super(type, time);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public WorldInformation(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		super(type, bitList, offset, bitSizeMap);
	}

	public abstract EntityID getEntityID();
}
