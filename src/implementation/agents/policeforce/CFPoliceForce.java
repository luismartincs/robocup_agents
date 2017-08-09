package implementation.agents.policeforce;

import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.messages.ACLPerformative;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.EnumSet;

public class CFPoliceForce extends CinvesAgent<PoliceForce> {

    @Override
    protected void postConnect() {
        super.postConnect();
    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time, changed, heard);

        for (ACLMessage msg : this.aclMessages) {

            ACLMessage aclMessage = msg;

            switch (aclMessage.getPerformative()){
                case CFP:
                    System.out.println(getID()+" get "+aclMessage.getPerformative()+" "+aclMessage.getConversationId());
                    ACLMessage message = new ACLMessage(time,getID(), ACLPerformative.REJECT_PROPOSAL,new EntityID(aclMessage.getSender()),aclMessage.getConversationId(),0);
                    addMessage(message);
                    System.out.println(getID()+" send "+message.getPerformative() +" "+aclMessage.getConversationId());
                    break;
            }
        }

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
