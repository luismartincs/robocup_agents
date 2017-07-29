import java.util.Collection;
import java.util.EnumSet;

import commlib.bdi.messages.ACLMessage;
import commlib.information.BuildingInformation;
import commlib.message.RCRSCSMessage;
import rescuecore2.Constants;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntityURN;

/**
 * A no-op agent.
 */
public class DummyRCRSCSAgent extends AbstractSampleRCRSCSAgent<PoliceForce> {

    private boolean channelComm;


    @Override
    public String toString() {
        return "Dummy Agent";
    }


    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.ROAD);
        boolean speakComm = config.getValue(Constants.COMMUNICATION_MODEL_KEY)
                .equals(ChannelCommunicationModel.class.getName());

        int numChannels = this.config.getIntValue("comms.channels.count");
        if ((speakComm) && (numChannels > 1)) {
            this.channelComm = true;
        } else {
            this.channelComm = false;
        }
    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            if (this.channelComm) {
                // Assign the agent to channel 1
                setMessageChannel(1);
                sendSubscribe(time, getMessageChannel());

            }
        }

        for(RCRSCSMessage msg : this.receivedMessageList){
            Logger.info(msg.toString());
            if(msg instanceof ACLMessage){
                System.out.println("Mensaje entrante acl " + ((ACLMessage)msg).getPerformative());
            }
        }

        sendMove(time, randomWalk());
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
    /*
    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE,
                StandardEntityURN.FIRE_STATION, StandardEntityURN.AMBULANCE_TEAM,
                StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_FORCE,
                StandardEntityURN.POLICE_OFFICE);
    }*/
}