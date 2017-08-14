
import commlib.message.RCRSCSMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import rescuecore2.Constants;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.messages.Message;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class CivilianAgent extends AbstractSampleRCRSCSAgent<Civilian> {

    private boolean channelComm;
    //ContractNet cnet;

    @Override
    public String toString() {
        return "Dummy Agent";
    }


    @Override
    protected void postConnect() {
        super.postConnect();
        System.out.println("post civil ---  id "+this.getID());
        //cnet=new ContractNet(this.getID(), 1);
        model.indexClass(StandardEntityURN.ROAD);
        boolean speakComm = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(ChannelCommunicationModel.class.getName());

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

        /**
         * SEND REQUEST
         */
        EntityID id=new EntityID(710989643);
       // msg=new ACLMessage(time,this.getID(),ACLPerformative.REQUEST,id);




       sendMove(time, randomDestination());
    }

    private void move(int time,EntityID agentID){
        // Plan a path to a blocked area

        System.out.println("Moving to "+agentID);
        List<EntityID> targets = new ArrayList<>();
        targets.add(agentID);
        List<EntityID> path = search.breadthFirstSearch(me().getPosition(),targets);

        if(path != null){
            Logger.info("Moving to target");
            sendMove(time, path);
            return;
        }
        Logger.debug("Couldn't plan a path to a blocked road");
        Logger.info("Moving randomly");
        sendMove(time, randomWalk());
    }



    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }
}
