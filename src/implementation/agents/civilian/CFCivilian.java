package implementation.agents.civilian;

import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityListBelief;
import commlib.cinvesframework.intention.GoToRefugePlan;
import commlib.cinvesframework.interaction.ContractNet;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class CFCivilian extends CinvesAgent<Civilian>{

    private final double BECOMES_VOLUNTEER = 0.20;

    private CivilianPlan plan;
    private GoToRefugePlan refugePlan;

    private int send = 0;

    @Override
    protected void postConnect() {
        super.postConnect();

        plan = new CivilianPlan(this);
        refugePlan = new GoToRefugePlan(this);

        Belief isVolunteer = new Belief();

        if(Math.random() < BECOMES_VOLUNTEER){
            isVolunteer.setDataBoolean(true);
            System.out.println(me()+" es voluntario");
        }else{
            isVolunteer.setDataBoolean(false);
        }

        getBeliefs().addBelief(BeliefType.REMOVE_BLOCKADES,isVolunteer);
    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        System.out.println("Do something");
        refugePlan.setTime(time);
        refugePlan.createPlan(getBeliefs(),getDesires());
    }


    /* @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time,changed,heard);
*/
        //getDesires().addDesire(DesireType.GOAL_LOCATION,new Desire(new EntityID(4335)));
/*
        List<EntityID> steps = plan.createPlan(getBeliefs(),getDesires());


        for (ACLMessage aclMessage:this.aclMessages) {

            ACLMessage queuedMessage = queuedMessages.get(aclMessage.getConversationId());

            if(queuedMessage != null) {
                boolean isValid = ContractNet.isValidState(queuedMessage.getPerformative(), aclMessage.getPerformative());

                if (aclMessage.getPerformative() == ACLPerformative.REJECT_PROPOSAL) {
                    System.out.println(getID() + " remove " + aclMessage.getConversationId());
                    queuedMessages.remove(aclMessage.getConversationId());
                }

                System.out.println(getID() + " get " + aclMessage.getPerformative() + " " + aclMessage.getConversationId() + " valid " + isValid);
            }
        }


        if(send < 10) {
            send++;
            int conversationId = nextConversationId();

            ACLMessage message = new ACLMessage(time, getID(), ACLPerformative.CFP, new EntityID(531016945), conversationId, 0);
            queuedMessages.put(conversationId, message);

            addMessage(message);

            System.out.println(getID()+" send "+message.getPerformative() + " "+conversationId);
        }

        sendMove(time,steps);
*/
 //   }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

}
