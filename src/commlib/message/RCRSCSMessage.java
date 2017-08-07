package commlib.message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import commlib.data.DataType;
import commlib.data.EntityIDData;
import commlib.data.EntityIDListData;
import commlib.data.RCRSCSData;
import commlib.data.ValueData;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;

/**
 * This class represent the transmitted message.
 * 
 * @author takefumi
 * 
 */
public abstract class RCRSCSMessage implements IMessage {
	protected BaseMessageType messageType;
	protected final static DataType[] COMMON_DATA_TYPE = new DataType[] { DataType.TIME };
	protected List<RCRSCSData<?>> data;

	/**
	 * <h2>Constructor</h2>
	 * 
	 * @param type
	 * @param time
	 */
	public RCRSCSMessage(BaseMessageType type, int time) {
		super();
		this.messageType = type;
		int len = COMMON_DATA_TYPE.length + type.getDataType().length;
		this.data = new ArrayList<RCRSCSData<?>>(len);
		for (int i = 0; i < len; i++) {
			this.data.add(null);
		}
		this.setData(new ValueData(DataType.TIME, time));
	}

	/**
	 * <h2>Constructor</h2>
	 * 
	 * @param data
	 * @param type
	 */
	public RCRSCSMessage(List<RCRSCSData<?>> data, BaseMessageType type) {
		super();
		this.messageType = type;
		this.data = data;
	}

	/**
	 * The method that the library use to convert the message.
	 * 
	 * @param type
	 * @param bitList
	 * @param offset
	 * @param bitSizeMap
	 */
	public RCRSCSMessage(BaseMessageType type, List<Integer> bitList,
			int offset, EnumMap<DataType, Integer> bitSizeMap) {
		this.messageType = type;
		int len = COMMON_DATA_TYPE.length + type.getDataType().length;
		this.data = new ArrayList<RCRSCSData<?>>(len);
		for (int i = 0; i < len; i++) {
			this.data.add(null);
		}
		EnumMap<DataType, Integer> counter = new EnumMap<DataType, Integer>(
				DataType.class);
		for (DataType dt : getDataTypeArray()) {
			int i = RCRSCSMessageConverter.getDataTypeIndex(counter, dt, 1);
			if (dt != DataType.ID_LIST && dt != DataType.AREA_LIST) {
				Integer bitSize = bitSizeMap.get(dt);
				int value = RCRSCSMessageConverter.bitToInt(bitList, offset,
						bitSize);
				// System.out.println(value);
				setData(DataType.createData(dt, value), i);
				offset += bitSize;
			} else {
				EntityIDListData d = DataType.createIDListData(dt);
				// get list size
				int listSize = RCRSCSMessageConverter.bitToInt(bitList, offset,
						32);
				offset += 32;
				int bitSize = 32;
				if (dt == DataType.AREA_LIST) {
					bitSize = bitSizeMap.get(DataType.AREA);
				}
				for (int j = 0; j < listSize; j++) {
					int value = RCRSCSMessageConverter.bitToInt(bitList,
							offset, bitSize);
					d.setData(new EntityID(value));
					offset += bitSize;
				}
				setData(d, i);
			}
		}
	}

	/**
	 * Return bit num of this message.
	 * 
	 * @param bitSizeMap
	 * @return ??????????????????<br>
	 *         bit num
	 */
	public int getMessageBitSize(EnumMap<DataType, Integer> bitSizeMap) {
		// int res = 0;
		// EnumMap<DataType, Integer> counter = new EnumMap<DataType, Integer>(
		// DataType.class);
		// for (DataType dt : getDataTypeArray()) {
		// if (dt == DataType.ID_LIST) {
		// EntityIDListData d = (EntityIDListData) this
		// .getData(dt, RCRSCSMessageConverter.getDataTypeIndex(
		// counter, dt, 1));
		// if (d != null) {
		// res += 32 * (d.getData().size() + 1);
		// }
		// } else if (dt == DataType.AREA_LIST) {
		// EntityIDListData d = (EntityIDListData) this
		// .getData(dt, RCRSCSMessageConverter.getDataTypeIndex(
		// counter, dt, 1));
		// if (d != null) {
		// res += (32 + (bitSizeMap.get(DataType.AREA) * d.getData()
		// .size()));
		// }
		// } else {
		// res += bitSizeMap.get(dt);
		// }
		// // System.out.println("??????:" + res);
		// }
		// // System.out.println("????????????" + res);
		// return res;
		int res = 0;
		for (RCRSCSData<?> d : this.data) {
			if (d != null) {
				DataType type = d.getType();
				if (type == DataType.AREA_LIST) {
					res += 32;
					EntityIDListData eild = (EntityIDListData) d;
					res += bitSizeMap.get(DataType.AREA)
							* eild.getData().size();
				} else if (type == DataType.ID_LIST) {
					EntityIDListData eild = (EntityIDListData) d;
					res += 32 * (eild.getData().size() + 1);
				} else {
					res += bitSizeMap.get(type);
				}
			}
		}
		return res;
	}

	@Override
	public List<RCRSCSData<?>> getData() {
		return new ArrayList<RCRSCSData<?>>(this.data);
	}

	@Override
	public void setData(RCRSCSData<?> data) {
		setData(data, 0);
	}

	public void setData(RCRSCSData<?> data, int index) {
		int targetIndex = 0;
		DataType dType = data.getType();
		// search common data
		for (int i = 0; i < COMMON_DATA_TYPE.length; i++) {
			if (COMMON_DATA_TYPE[i] == data.getType()) {
				if (targetIndex == index) {
					this.data.set(i, data);
					return;
				} else {
					targetIndex++;
				}
			}
		}
		DataType[] necessary = this.messageType.getDataType();
		// search necessary data
		for (int i = 0; i < necessary.length; i++) {
			if (necessary[i] == dType) {
				if (targetIndex == index) {
					this.data.set(COMMON_DATA_TYPE.length + i, data);
					return;
				} else {
					targetIndex++;
				}
			}
		}

		System.err.println("illegal argument to set message value:" + dType
				+ "," + index + ",data=" + data + " " + this.getClass());
		new Exception().printStackTrace();
	}

	/**
	 * This method confirm whether data necessary for sending message are
	 * existing.<br>
	 * If not, return false.
	 * 
	 * @return this message is senndable : true<br>
	 *         otherwise : false
	 */
	public boolean isSendable() {
		boolean res = true;
		for (int i = 0; i < this.messageType.getDataType().length; i++) {
			if (this.data.get(i) == null) {
				res = false;
				break;
			}
		}
		return res;
	}

	/**
	 * Return type of this message.
	 * 
	 * @return type of message
	 */
	public BaseMessageType getMessageType() {
		return this.messageType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result
				+ ((messageType == null) ? 0 : messageType.hashCode());
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
		RCRSCSMessage other = (RCRSCSMessage) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (messageType == null) {
			if (other.messageType != null)
				return false;
		} else if (!messageType.equals(other.messageType))
			return false;
		return true;
	}

	/**
	 * Return data that have specified DataType and index.<br>
	 * This method is only used in the class extending this class.
	 * 
	 * @param dType
	 * @param index
	 * @return
	 */
	protected RCRSCSData<?> getData(DataType dType, int index) {
		int targetIndex = 0;
		for (int i = 0; i < COMMON_DATA_TYPE.length; i++) {
			if (COMMON_DATA_TYPE[i] == dType) {
				if (targetIndex == index) {
					return this.data.get(index);
				} else {
					targetIndex++;
				}
			}
		}
		DataType[] necessary = this.messageType.getDataType();
		// search necessary data
		for (int i = 0; i < necessary.length; i++) {
			if (necessary[i] == dType) {
				if (targetIndex == index) {
					return this.data.get(COMMON_DATA_TYPE.length + i);
				} else {
					targetIndex++;
				}
			}
		}

		System.err.println("illegal argument to get message value:" + dType
				+ "," + index);
		new Exception().printStackTrace();
		return null;
	}

	/**
	 * Return data(EntityID) that have specified DataType and index.<br>
	 * This method is only used in the class extending this class.
	 * 
	 * @param dType
	 * @param index
	 * @return EntityID
	 */
	protected EntityID getID(DataType dType, int index) {
		EntityID res = null;
		RCRSCSData<?> d = this.getData(dType, index);
		if (d != null) {
			if (d instanceof EntityIDData) {
				res = ((EntityIDData) d).getData();
			} else {
				Integer id = ((ValueData) d).getData();
				if (id != null) {
					res = new EntityID(id);
				}
			}
		}
		return res;
	}

	/**
	 * @param index
	 * @return
	 */
	protected int getHP(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.HP, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	/**
	 * Return stem num that the message is created.
	 * 
	 * @param index
	 * @return
	 */
	protected int getSendTime(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.TIME, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	/**
	 * Return stem num that the message is created.
	 * 
	 * @return stem num
	 */
	public int getSendTime() {
		return this.getSendTime(0);
	}

	protected int getDamage(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.DAMAGE, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	protected int getBuriedness(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.BURIEDNESS, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	protected int getFieryness(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.FIERYNESS, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	protected int getWaterPower(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.WATER_POWER, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	protected int getWater(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.WATER, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	// protected int getSupplyQuantity(int index) {
	// int res = -1;
	// RCRSCSData<?> d = this.getData(DataType.SUPPLY_QUANTITY, index);
	// if (d != null) {
	// Integer id = ((ValueData) d).getData();
	// if (id != null) {
	// res = id;
	// }
	// }
	// return res;
	// }

	protected int getRepairCost(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.REPAIR_COST, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	protected int getBrokenness(int index) {
		int res = -1;
		RCRSCSData<?> d = this.getData(DataType.BROKENNESS, index);
		if (d != null) {
			Integer id = ((ValueData) d).getData();
			if (id != null) {
				res = id;
			}
		}
		return res;
	}

	protected Pair<Integer, Integer> getCoodinate(int index) {
		Integer x = null;
		Integer y = null;
		Pair<Integer, Integer> res = null;
		RCRSCSData<?> d = this.getData(DataType.X_COORDINATE, index);
		if (d != null) {
			x = ((ValueData) d).getData();
		}
		d = this.getData(DataType.Y_COORDINATE, index);
		if (d != null) {
			y = ((ValueData) d).getData();
		}
		res = new Pair<Integer, Integer>(x, y);
		return res;
	}

	protected List<EntityID> getEntityIDList(DataType type, int index) {
		List<EntityID> res = new ArrayList<EntityID>();
		RCRSCSData<?> d = this.getData(type, index);
		if (d != null) {
			if (d instanceof EntityIDListData) {
				res = ((EntityIDListData) d).getData();
			}
			// else {
			// for (Integer id : ((ListData) d).getData()) {
			// if (id != null) {
			// res.add(new EntityID(id));
			// }
			// }
			// }
		}
		return res;
	}

	protected void setCoorinate(Pair<Integer, Integer> cor) {
		this.setData(new ValueData(DataType.X_COORDINATE, cor.first()));
		this.setData(new ValueData(DataType.Y_COORDINATE, cor.second()));
	}

	protected DataType[] getDataTypeArray() {
		DataType[] res = new DataType[data.size()];
		int len = COMMON_DATA_TYPE.length;
		System.arraycopy(COMMON_DATA_TYPE, 0, res, 0, len);
		System.arraycopy(this.messageType.getDataType(), 0, res, len,
				this.messageType.getDataType().length);
		return res;
	}
}
