package commlib.data;

import java.util.ArrayList;
import java.util.List;

@Deprecated
class ListData extends RCRSCSData<List<Integer>> {

	public ListData(DataType type) {
		super(type);
		this.value = new ArrayList<Integer>();
	}

	// public ListData(DataType type, List<Integer> value) {
	// super(type, value);
	// }

	@Override
	public void setData(List<Integer> obj) {
		this.value = obj;
	}

	public void setData(Integer value) {
		this.value.add(value);
	}
}
