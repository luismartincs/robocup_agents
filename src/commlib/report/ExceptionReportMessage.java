package commlib.report;

import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.message.BaseMessageType;

import rescuecore2.worldmodel.EntityID;

/**
 * This class is the Message Class that report the given task is not able to
 * complete at the moment.
 * 
 * @author takefumi
 * 
 */
public class ExceptionReportMessage extends ReportMessage {
	/**
	 * <h2>Constructor</h2> Create the message that report the given task is
	 * uncompletable.<br>
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
	public ExceptionReportMessage(int time, EntityID platoonID) {
		super(BaseMessageType.EXCEPTION, time, platoonID);
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public ExceptionReportMessage(List<Integer> bitList, int offset,
			EnumMap<DataType, Integer> bitSizeMap) {
		super(BaseMessageType.EXCEPTION, bitList, offset, bitSizeMap);
	}

}
