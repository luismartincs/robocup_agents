package commlib.report;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * The DoneReportMessage report that the given instruction is completed.
 * 
 * @author takefumi
 * 
 */
public class DoneReportMessage extends ReportMessage {

	/**
	 * <h2>Constructor</h2> Create the message that report the completion of
	 * given task. <br>
	 * Included data are follow.<br>
	 * <ul>
	 * <li>time:the time that the message is created.(int)</li>
	 * <li>platoonID:EntityID of the rescue agent</li>
	 * </ul>
	 * 
	 * @param time
	 *            step num
	 * @param platoonID
	 *            EntityID of the rescue agent.
	 */
	public DoneReportMessage(int time, EntityID platoonID) {
		super(BaseMessageType.DONE, time, platoonID);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public DoneReportMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.DONE, bitList, offset, bitSizeMap);
	}

}
