package implementation.agents.civilian;

import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityMapBelief;
import commlib.cinvesframework.belief.EnvironmentBelief;
import commlib.cinvesframework.intention.GoToRefugePlan;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.utils.GeneralUtils;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;

public class CFCivilian extends CinvesAgent<Civilian>{

    private final double BECOMES_VOLUNTEER = 0.20;

    private CivilianPlan plan;
    private GoToRefugePlan refugePlan;

    @Override
    protected void postConnect() {
        super.postConnect();

        plan = new CivilianPlan(this);
        refugePlan = new GoToRefugePlan(this);

        Belief isVolunteer = new Belief();

        if(Math.random() < BECOMES_VOLUNTEER){
            isVolunteer.setDataBoolean(true);
        }else{
            isVolunteer.setDataBoolean(false);
        }

        getBeliefs().addBelief(BeliefType.VOLUNTEER,isVolunteer);
        getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());
    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        refugePlan.setTime(time);
        refugePlan.createPlan(getBeliefs(),getDesires());

        /*
         Hacer esto un plan y agregar el reporte de la victima

        for(Civilian victim: GeneralUtils.getCivilianAround(this,changed)){
            if(victim.isPositionDefined() & victim.isHPDefined()
                    & victim.isBuriednessDefined() & victim.isDamageDefined()){
                if(victim.getDamage() > 0 || victim.getBuriedness()>0) {
                    System.out.println("There is victim in " + victim.getPosition());
                }
            }
        }

        */
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
