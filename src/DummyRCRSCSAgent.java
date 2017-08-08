import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import commlib.bdi.messages.ACLMessage;
import commlib.information.BuildingInformation;
import commlib.message.RCRSCSMessage;
import commlib.task.RestTaskMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import rescuecore2.Constants;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

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
            if(msg instanceof ACLMessage){

                System.out.println("Mensaje entrante acl en police "+ me().getID()+" , " + ((ACLMessage) msg).getEntityID());
                //move(time,aclMessage.getTargetAgentID());
            }
        }

        sendMove(time, randomWalk());
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
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE,
                StandardEntityURN.FIRE_STATION, StandardEntityURN.AMBULANCE_TEAM,
                StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_FORCE,
                StandardEntityURN.POLICE_OFFICE);
    }
}