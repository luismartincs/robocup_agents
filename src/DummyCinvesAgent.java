import commlib.bdi.messages.ACLMessage;
import commlib.bdi.messages.ACLPerformative;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.LocationBelief;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.message.RCRSCSMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class DummyCinvesAgent extends CinvesAgent<Civilian>{

    @Override
    protected void postConnect() {
        super.postConnect();

        getDesires().addDesire(DesireType.GOAL_LOCATION,new Desire(new EntityID(283444)));

    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time,changed,heard);

        Desire lb = (Desire) getDesires().getDesire(DesireType.GOAL_LOCATION);

        /*
        for(RCRSCSMessage msg : this.receivedMessageList){
            if(msg instanceof ACLMessage){

                System.out.println("Mensaje entrante acl "+ me().getID()+" , " + ((ACLMessage) msg).getEntityID());
                //move(time,aclMessage.getTargetAgentID());
            }
        }*/

        addMessage(new ACLMessage(time,getID(), ACLPerformative.CFP,new EntityID(531016945)));


    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

}
