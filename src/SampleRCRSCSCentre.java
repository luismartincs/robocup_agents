import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import commlib.bdi.messages.ACLMessage;
import commlib.bdi.messages.ACLPerformative;
import commlib.data.DataType;
import commlib.message.BaseMessageType;
import commlib.message.RCRSCSMessage;
import commlib.components.AbstractCSAgent;
import commlib.task.RestTaskMessage;
import commlib.task.TaskMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import rescuecore2.Constants;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.EntityID;

/**
 * A sample centre agent.
 */
public class SampleRCRSCSCentre extends AbstractCSAgent<PoliceOffice>{
	
	private boolean	channelComm;
	
	
	@Override
	public String toString(){
		return "Sample centre";
	}
	
	
	@Override
	public void postConnect(){
		super.postConnect();
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

//32737

		//addMessage(new ACLMessage(time,getID(), ACLPerformative.CFP,new EntityID()));

		addMessage(new ClearRouteTaskMessage(time, this.getID(),new EntityID(531016945),new EntityID(32737),new EntityID(32737)));



		for(RCRSCSMessage msg : this.receivedMessageList){
			Logger.info(msg.toString());
		}


	}
	
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_STATION,
				StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_OFFICE,StandardEntityURN.POLICE_FORCE);
	}
}