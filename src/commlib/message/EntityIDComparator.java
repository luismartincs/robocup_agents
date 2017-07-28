package commlib.message;

import java.util.Comparator;

import rescuecore2.worldmodel.EntityID;

/**
 * The EntityIDComparator is Comparator class to sort in ascending order.
 * 
 * @author takefumi
 * 
 */
class EntityIDComparator implements Comparator<EntityID> {
	@Override
	public int compare(EntityID o1, EntityID o2) {
		return o1.getValue() - o2.getValue();
	}
}
