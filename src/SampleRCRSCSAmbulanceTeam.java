import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;

/**
 * A sample rcrscs ambulance team agent.
 */
public class SampleRCRSCSAmbulanceTeam extends
		AbstractSampleRCRSCSAgent<AmbulanceTeam>{
	
	private Collection<EntityID>	unexploredBuildings;
	
	private boolean								channelComm;
	
	
	@Override
	public String toString(){
		return "Sample RCRSCS Ambulance Team";
	}
	
	
	@Override
	protected void postConnect(){
		super.postConnect();
		model.indexClass(StandardEntityURN.CIVILIAN,
				StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE,
				StandardEntityURN.BUILDING);
		unexploredBuildings = new HashSet<EntityID>(buildingIDs);
		
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
		}
		
		updateUnexploredBuildings(changed);
		// Am I transporting a civilian to a refuge?
		if(someoneOnBoard()){
			// Am I at a refuge?
			if(location() instanceof Refuge){
				// Unload!
				sendUnload(time);
				return;
			}else{
				// Move to a refuge
				List<EntityID> path = search.breadthFirstSearch(me().getPosition(),
						refugeIDs);
				if(path != null){
					Logger.info("Moving to refuge");
					sendMove(time, path);
					return;
				}
				// What do I do now? Might as well carry on and see if we can dig
				// someone else out.
				Logger.debug("Failed to plan path to refuge");
			}
		}
		// Go through targets (sorted by distance) and check for things we can do
		for(Human next : getTargets()){
			if(next.getPosition().equals(location().getID())){
				// Targets in the same place might need rescueing or loading
				if((next instanceof Civilian) && next.getBuriedness() == 0
						&& !(location() instanceof Refuge)){
					// Load
					Logger.info("Loading " + next);
					sendLoad(time, next.getID());
					return;
				}
				if(next.getBuriedness() > 0){
					// Rescue
					Logger.info("Rescueing " + next);
					sendRescue(time, next.getID());
					return;
				}
			}else{
				// Try to move to the target
				List<EntityID> path = search.breadthFirstSearch(me().getPosition(),
						next.getPosition());
				if(path != null){
					Logger.info("Moving to target");
					sendMove(time, path);
					return;
				}
			}
		}
		// Nothing to do
		List<EntityID> path = search.breadthFirstSearch(me().getPosition(),
				unexploredBuildings);
		if(path != null){
			Logger.info("Searching buildings");
			sendMove(time, path);
			return;
		}
		Logger.info("Moving randomly");
		sendMove(time, randomWalk());
	}
	
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}
	
	
	private boolean someoneOnBoard(){
		for(StandardEntity next : model
				.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
			if(((Human) next).getPosition().equals(getID())){
				Logger.debug(next + " is on board");
				return true;
			}
		}
		return false;
	}
	
	
	private List<Human> getTargets(){
		List<Human> targets = new ArrayList<Human>();
		for(StandardEntity next : model.getEntitiesOfType(
				StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE,
				StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM)){
			Human h = (Human) next;
			if(h == me()){
				continue;
			}
			if(h.isHPDefined() && h.isBuriednessDefined() && h.isDamageDefined()
					&& h.isPositionDefined() && h.getHP() > 0
					&& (h.getBuriedness() > 0 || h.getDamage() > 0)){
				targets.add(h);
			}
		}
		Collections.sort(targets, new DistanceRCRSCSSorter(location(), model));
		return targets;
	}
	
	
	private void updateUnexploredBuildings(ChangeSet changed){
		for(EntityID next : changed.getChangedEntities()){
			unexploredBuildings.remove(next);
		}
	}
}