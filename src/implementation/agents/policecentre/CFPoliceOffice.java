package implementation.agents.policecentre;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.message.RCRSCSMessage;
import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;

public class CFPoliceOffice extends CinvesAgent<PoliceOffice> {

    private boolean							channelComm;
    BlockadeList blockadeList;

    @Override
    public void postConnect(){
        System.out.println("hola soy un police office "+this.getID());
        blockadeList=new BlockadeList();
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
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
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
                if(((ACLMessage) msg).getPerformative().equals(ACLPerformative.INFORM)){
                    BlockadeInfo binfo=new BlockadeInfo(((ACLMessage) msg).getXPosition(),((ACLMessage) msg).getYPosition(),((ACLMessage) msg).getRepairCost(),((ACLMessage) msg).getBlockade());
                    blockadeList.addBlockade(binfo);
                    System.out.println("agregada información de blockade ");
                }
            }
        }

    }
    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_STATION,
                StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_OFFICE,StandardEntityURN.POLICE_FORCE);
    }
}
