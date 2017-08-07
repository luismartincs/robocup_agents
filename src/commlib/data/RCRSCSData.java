package commlib.data;

/**
 * This class show that Data of Entity, and have one {@link DataType} and one
 * value.
 * 
 * @author takefumi
 * 
 * @param <E>
 */
public abstract class RCRSCSData<E extends Object> {
	/**
	 * type of this data.
	 */
	protected DataType type;
	/**
	 * actual value of this data.
	 */
	protected E value;

	/**
	 * ?????????????????????<br>
	 * Constructor
	 * 
	 * @param type
	 *            {@link DataType}
	 */
	RCRSCSData(DataType type) {
		this.type = type;
		this.value = null;
	}

	// RCRSCSData(DataType type, E value) {
	// this.type = type;
	// this.value = null;
	// }

	/**
	 * Return the {@link DataType} of this data.
	 * 
	 * @return {@link DataType}
	 */
	public DataType getType() {
		return this.type;
	}

	/**
	 * Return the actual value of this data.
	 * 
	 * @return
	 */
	public E getData() {
		return this.value;
	}

	/**
	 * Set the value to obj
	 * 
	 * @param obj
	 *            actual value of this data
	 */
	public abstract void setData(E obj);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RCRSCSData other = (RCRSCSData) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.type.toString() + "[" + this.value.toString() + "]";
	}
}
