package implementation.agents.firebrigade;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import implementation.agents.Quadrant;
import implementation.agents.policeforce.LeaderElectionPlan;
import implementation.agents.policeforce.PolicePlan;
import implementation.agents.policeforce.RequestReplyPlan;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;

public class CFFireBrigade extends CinvesAgent<PoliceForce> {

    //private PolicePlan policePlan;//esto se va y se pone un fireBrigadePlan
    private LeaderElectionPlan leaderElectionPlan;

    private RequestReplyPlan requestReplyPlan;

    @Override
    protected void postConnect() {
        super.postConnect();

        System.out.println("conectado Fire Brigade "+this.getID()+"     "+"[quadrant : "+quadrant+"]");

        //policePlan = new PolicePlan(this); //NOTE: este cambia por el FireBrigadePlan.
        leaderElectionPlan = new LeaderElectionPlan(this);
        requestReplyPlan = new RequestReplyPlan(this);


        Belief removeBlockades = new Belief();
        removeBlockades.setDataBoolean(true);

        getBeliefs().addBelief(BeliefType.VOLUNTEER,removeBlockades);
        //getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());//NOTE: Según yo esto ya no va.

        getBeliefs().addBelief(BeliefType.REPORTED_FIRES,new EntityMapBelief());

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        /*
        policePlan.setTime(time);
        policePlan.createPlan(getBeliefs(),getDesires());
        */

        leaderElectionPlan.setTime(time);

        Object leaderElected = leaderElectionPlan.createPlan(getBeliefs(),getDesires());

        if(leaderElected != null){

            /**
             * Si eres el lider del cuadrante actualizas tus creencias sobre los edificios que te corresponden
             */

            if(leaderElectionPlan.imLeader()) {

                if (getBeliefs().getBelief(BeliefType.BUILDINGS_IN_QUADRANT) == null) {

                    EntityListBelief buildingsInQuadrant = new EntityListBelief();
                    EntityListBelief buildings = (EntityListBelief) getBeliefs().getBelief(BeliefType.BUILDINGS);

                    for (StandardEntity building : buildings.getEntities()) {

                        Pair<Integer, Integer> point = building.getLocation(getWorldModel());

                        int px = point.first();
                        int py = point.second();
                        int q = Quadrant.getQuadrant(getWorldModel(), px, py);

                        if (q == quadrant) {
                            buildingsInQuadrant.addEntity(building);
                        }

                    }
                    getBeliefs().addBelief(BeliefType.BUILDINGS_IN_QUADRANT, buildingsInQuadrant);
                }

            }

            requestReplyPlan.setTime(time);
            requestReplyPlan.createPlan(getBeliefs(),getDesires());


            /*
                EntityListBelief biq = (EntityListBelief)getBeliefs().getBelief(BeliefType.BUILDINGS_IN_QUADRANT);

                EntityID position = biq.getEntities().get(0).getID();//new EntityID(28953);
                getDesires().addDesire(DesireType.GOAL_LOCATION, new Desire(position));
                Desire goalLocation = getDesires().getDesire(DesireType.GOAL_LOCATION);
                EntityID myposition = ((Human) me()).getPosition();

                if (goalLocation.getEntityID().getValue() == myposition.getValue()) {
                    biq.getEntities().remove(0);
                    sendRest(time);
                } else {
                    List<EntityID> path = sp.createPlan(getBeliefs(), getDesires());
                    sendMove(time, path);
                }*/

        }

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
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }
}
