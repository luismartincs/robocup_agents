package implementation.agents.policeforce;

import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityMapBelief;
import commlib.cinvesframework.belief.EnvironmentBelief;
import commlib.cinvesframework.agent.CinvesAgent;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;

public class CFPoliceForce extends CinvesAgent<PoliceForce> {


    private PolicePlan policePlan;

    @Override
    protected void postConnect() {
        super.postConnect();
        System.out.println("conectado police force "+this.getID());
        policePlan = new PolicePlan(this);

        Belief removeBlockades = new Belief();
        removeBlockades.setDataBoolean(true);

        getBeliefs().addBelief(BeliefType.VOLUNTEER,removeBlockades);
        getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        policePlan.setTime(time);
        policePlan.createPlan(getBeliefs(),getDesires());

    }


    /*
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

    }*/

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
