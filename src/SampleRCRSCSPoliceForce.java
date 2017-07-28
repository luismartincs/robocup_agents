import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import commlib.message.RCRSCSMessage;
import commlib.information.BlockadeInformation;
import commlib.information.BuildingInformation;
import commlib.information.PositionInformation;
import commlib.information.VictimInformation;
import rescuecore2.Constants;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;

/**
 * A sample police force agent.
 */
public class SampleRCRSCSPoliceForce extends
		AbstractSampleRCRSCSAgent<PoliceForce>{
	
	private static final String	DISTANCE_KEY	= "clear.repair.distance";
	
	private int									distance;
	
	private boolean							channelComm;
	
	
	@Override
	public String toString(){
		return "Sample police force";
	}
	
	
	@Override
	protected void postConnect(){
		super.postConnect();
		model.indexClass(StandardEntityURN.ROAD);
		distance = config.getIntValue(DISTANCE_KEY);
		
		boolean speakComm = config.getValue(Constants.COMMUNICATION_MODEL_KEY)
				.equals(ChannelCommunicationModel.class.getName());
		
		int numChannels = this.config.getIntValue("comms.channels.count");
		if((speakComm) && (numChannels > 1)){
			this.channelComm = true;
		}else{
			this.channelComm = false;
		}
	}
	
	
	@Override
	protected void thinking(int time, ChangeSet changed, Collection<Command> heard){
		if(time == config
				.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			if(this.channelComm){
				// Assign the agent to channel 1
				setMessageChannel(1);
				sendSubscribe(time,getMessageChannel());
			}
		}
		
		// Agent send its position
		PositionInformation position = new PositionInformation(time, me().getID(),
				me().getLocation(this.model));
		addMessage(position);
		
		// Send the Blockade, Building and Victims percepted in its radius
		StandardEntity entity;
		BlockadeInformation blockadeInfo;
		BuildingInformation buildingInfo;
		VictimInformation victimInfo;
		for(EntityID id : changed.getChangedEntities()){
			entity = this.model.getEntity(id);
			if(entity instanceof Blockade){
				Blockade blockade = (Blockade) entity;
				if(blockade.isPositionDefined() & blockade.isRepairCostDefined()){
					blockadeInfo = new BlockadeInformation(time, blockade.getID(),
							blockade.getPosition(), blockade.getRepairCost());
					addMessage(blockadeInfo);
				}
			}else if(entity instanceof Building){
				Building building = (Building) entity;
				if(building.isFierynessDefined() & building.isBrokennessDefined()){
					buildingInfo = new BuildingInformation(time, building.getID(),
							building.getFieryness(), building.getBrokenness());
					addMessage(buildingInfo);
				}
			}else if(entity instanceof Civilian){
				Civilian victim = (Civilian) entity;
				if(victim.isPositionDefined() & victim.isHPDefined()
						& victim.isBuriednessDefined() & victim.isDamageDefined()){
					victimInfo = new VictimInformation(time, victim.getID(),
							victim.getPosition(), victim.getHP(), victim.getBuriedness(),
							victim.getDamage());
					addMessage(victimInfo);
				}
			}
		}
		
		// Print the received messages from the common channel
		for(RCRSCSMessage msg : this.receivedMessageList){
			Logger.info(msg.toString());
			if(msg instanceof PositionInformation){
				System.out.println("Mensaje entrante " + ((PositionInformation)msg).getCoordinate());
			}
		}
		
		// Am I near a blockade?
		Blockade target = getTargetBlockade();
		if(target != null){
			Logger.info("Clearing blockade " + target);
			sendSpeak(time, 1, ("Clearing " + target).getBytes());
			sendClear(time, target.getID());
			return;
		}
		// Plan a path to a blocked area
		List<EntityID> path = search.breadthFirstSearch(me().getPosition(),
				getBlockedRoads());
		if(path != null){
			Logger.info("Moving to target");
			Road r = (Road) model.getEntity(path.get(path.size() - 1));
			Blockade b = getTargetBlockade(r, -1);
			sendMove(time, path, b.getX(), b.getY());
			Logger.debug("Path: " + path);
			Logger.debug("Target coordinates: " + b.getX() + ", " + b.getY());
			return;
		}
		Logger.debug("Couldn't plan a path to a blocked road");
		Logger.info("Moving randomly");
		sendMove(time, randomWalk());
	}
	
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
	}
	
	
	private List<EntityID> getBlockedRoads(){
		Collection<StandardEntity> e = model
				.getEntitiesOfType(StandardEntityURN.ROAD);
		List<EntityID> result = new ArrayList<EntityID>();
		for(StandardEntity next : e){
			Road r = (Road) next;
			if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
				result.add(r.getID());
			}
		}
		return result;
	}
	
	
	private Blockade getTargetBlockade(){
		Logger.debug("Looking for target blockade");
		Area location = (Area) location();
		Logger.debug("Looking in current location");
		Blockade result = getTargetBlockade(location, distance);
		if(result != null){
			return result;
		}
		Logger.debug("Looking in neighbouring locations");
		for(EntityID next : location.getNeighbours()){
			location = (Area) model.getEntity(next);
			result = getTargetBlockade(location, distance);
			if(result != null){
				return result;
			}
		}
		return null;
	}
	
	
	private Blockade getTargetBlockade(Area area, int maxDistance){
		// Logger.debug("Looking for nearest blockade in " + area);
		if(area == null || !area.isBlockadesDefined()){
			// Logger.debug("Blockades undefined");
			return null;
		}
		List<EntityID> ids = area.getBlockades();
		// Find the first blockade that is in range.
		int x = me().getX();
		int y = me().getY();
		for(EntityID next : ids){
			Blockade b = (Blockade) model.getEntity(next);
			double d = findDistanceTo(b, x, y);
			// Logger.debug("Distance to " + b + " = " + d);
			if(maxDistance < 0 || d < maxDistance){
				// Logger.debug("In range");
				return b;
			}
		}
		// Logger.debug("No blockades in range");
		return null;
	}
	
	
	private int findDistanceTo(Blockade b, int x, int y){
		// Logger.debug("Finding distance to " + b + " from " + x + ", " + y);
		List<Line2D> lines = GeometryTools2D.pointsToLines(
				GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
		double best = Double.MAX_VALUE;
		Point2D origin = new Point2D(x, y);
		for(Line2D next : lines){
			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			// Logger.debug("Next line: " + next + ", closest point: " + closest +
			// ", distance: " + d);
			if(d < best){
				best = d;
				// Logger.debug("New best distance");
			}
			
		}
		return (int) best;
	}
	
	/**
	 * Get the blockade that is nearest this agent.
	 * 
	 * @return The EntityID of the nearest blockade, or null if there are no
	 *         blockades in the agents current location.
	 */
	/*
	 * public EntityID getNearestBlockade() {
	 * return getNearestBlockade((Area)location(), me().getX(), me().getY());
	 * }
	 */
	
	/**
	 * Get the blockade that is nearest a point.
	 * 
	 * @param area
	 *          The area to check.
	 * @param x
	 *          The X coordinate to look up.
	 * @param y
	 *          The X coordinate to look up.
	 * @return The EntityID of the nearest blockade, or null if there are no
	 *         blockades in this area.
	 */
	/*
	 * public EntityID getNearestBlockade(Area area, int x, int y) {
	 * double bestDistance = 0;
	 * EntityID best = null;
	 * Logger.debug("Finding nearest blockade");
	 * if (area.isBlockadesDefined()) {
	 * for (EntityID blockadeID : area.getBlockades()) {
	 * Logger.debug("Checking " + blockadeID);
	 * StandardEntity entity = model.getEntity(blockadeID);
	 * Logger.debug("Found " + entity);
	 * if (entity == null) {
	 * continue;
	 * }
	 * Pair<Integer, Integer> location = entity.getLocation(model);
	 * Logger.debug("Location: " + location);
	 * if (location == null) {
	 * continue;
	 * }
	 * double dx = location.first() - x;
	 * double dy = location.second() - y;
	 * double distance = Math.hypot(dx, dy);
	 * if (best == null || distance < bestDistance) {
	 * bestDistance = distance;
	 * best = entity.getID();
	 * }
	 * }
	 * }
	 * Logger.debug("Nearest blockade: " + best);
	 * return best;
	 * }
	 */
}
