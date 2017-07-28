package commlib.data;

/**
 * This class represent the data that express Integer value.
 * 
 * @author takefumi
 * 
 */
public class ValueData extends RCRSCSData<Integer> {
	/**
	 * Constructor
	 * 
	 * @param type
	 *            type of the data({@link DataType})
	 */
	public ValueData(DataType type) {
		super(type);
		this.value = null;
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            type of the data({@link DataType})
	 * @param value
	 *            actual value of the data({@link Integer})
	 */
	public ValueData(DataType type, Integer value) {
		super(type);
		this.value = value;
	}

	@Override
	public void setData(Integer value) {
		this.value = value;
	}

}
