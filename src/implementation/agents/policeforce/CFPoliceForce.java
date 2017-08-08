package implementation.agents.policeforce;

import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.agent.CinvesAgent;
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

            System.out.println("Mensaje entrante acl " + me().getID() + " , " + aclMessage.getSender() + " " + aclMessage.getReceiver() +" "+aclMessage.getConversationId()+" "+aclMessage.getContent());
            System.out.println(getWorldModel().getEntity(new EntityID(aclMessage.getSender())));
            System.out.println(getWorldModel().getEntitiesOfType(StandardEntityURN.CIVILIAN).size());

        }

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
