import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import commlib.cinvesframework.messages.ACLMessage;
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
		System.out.println("hola soy un police centre "+this.getID());
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


		for(RCRSCSMessage msg : this.receivedMessageList){
			if(msg instanceof ACLMessage){
				System.out.println("recibo mensaje de "+((ACLMessage) msg).getSender());
			}
		}


	}
	
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_STATION,
				StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_OFFICE,StandardEntityURN.POLICE_FORCE);
	}
}