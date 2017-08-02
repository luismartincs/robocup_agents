import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import commlib.components.AbstractCSAgent;
import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Human;

/**
 * Abstract base class for sample agents.
 * 
 * @param <E>
 *          The subclass of StandardEntity this agent wants to control.
 */
public abstract class AbstractSampleRCRSCSAgent<E extends StandardEntity>
		extends AbstractCSAgent<E>{
	
	private static final int							RANDOM_WALK_LENGTH	= 100;
	
	/**
	 * The search algorithm.
	 */
	protected SampleRCRSCSSearch					search;
	
	/**
	 * Whether to use AKSpeak messages or not.
	 */
	protected boolean											useSpeak;
	
	/**
	 * Cache of building IDs.
	 */
	protected List<EntityID>							buildingIDs;
	
	/**
	 * Cache of road IDs.
	 */
	protected List<EntityID>							roadIDs;
	
	/**
	 * Cache of refuge IDs.
	 */
	protected List<EntityID>							refugeIDs;
	
	private Map<EntityID, Set<EntityID>>	neighbours;
	
	
	/**
	 * Construct an AbstractRCRSCSSampleAgent.
	 */
	protected AbstractSampleRCRSCSAgent(){
	}
	
	
	@Override
	protected void postConnect(){
		super.postConnect();
		buildingIDs = new ArrayList<EntityID>();
		roadIDs = new ArrayList<EntityID>();
		refugeIDs = new ArrayList<EntityID>();
		
		for(StandardEntity next : model){
			if(next instanceof Building){
				buildingIDs.add(next.getID());
			}
			if(next instanceof Road){
				roadIDs.add(next.getID());
			}
			if(next instanceof Refuge){
				refugeIDs.add(next.getID());
			}
		}
		
		search = new SampleRCRSCSSearch(model);
		neighbours = search.getGraph();
	}
	
	
	/**
	 * Construct a random walk starting from this agent's current location to a
	 * random building.
	 * 
	 * @return A random walk.
	 */
	protected List<EntityID> randomWalk(){
		List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
		Set<EntityID> seen = new HashSet<EntityID>();
		EntityID current = ((Human) me()).getPosition();
		for(int i = 0; i < RANDOM_WALK_LENGTH; ++i){
			result.add(current);
			seen.add(current);
			List<EntityID> possible = new ArrayList<EntityID>(neighbours.get(current));
			Collections.shuffle(possible, random);
			boolean found = false;
			for(EntityID next : possible){
				if(seen.contains(next)){
					continue;
				}
				current = next;
				found = true;
				break;
			}
			if(!found){
				// We reached a dead-end.
				break;
			}
		}
		return result;
	}

	protected List<EntityID> randomDestination(){

		List<EntityID> targets = new ArrayList<>();
		targets.add(buildingIDs.get((int)Math.random()*buildingIDs.size()));
		EntityID current = ((Human) me()).getPosition();
		List<EntityID> path = search.breadthFirstSearch(current,targets);

		return path;
	}
}