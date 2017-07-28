import java.util.Collection;
import java.util.EnumSet;
import commlib.components.AbstractCSAgent;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

/**
 * A no-op agent.
 */
public class DummyRCRSCSAgent extends AbstractCSAgent<StandardEntity>{
	
	@Override
	protected void thinking(int time, ChangeSet changed, Collection<Command> heard){
		sendRest(time);
	}
	
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE,
				StandardEntityURN.FIRE_STATION, StandardEntityURN.AMBULANCE_TEAM,
				StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.POLICE_OFFICE);
	}
}