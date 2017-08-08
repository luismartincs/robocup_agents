package implementation.agents.policeforce;

import commlib.bdi.messages.ACLMessage;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.message.RCRSCSMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import implementation.agents.civilian.CivilianPlan;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class CFPoliceForce extends CinvesAgent<PoliceForce>{

    @Override
    protected void postConnect() {
        super.postConnect();
    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time,changed,heard);

        /*
        for(RCRSCSMessage msg : this.receivedMessageList){
            if(msg instanceof ACLMessage){

                ACLMessage aclMessage = (ACLMessage)msg;

                System.out.println("Mensaje entrante acl "+ me().getID()+" , " + aclMessage.getAgentID());
                //move(time,aclMessage.getTargetAgentID());
            }
        }*/

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
