package implementation.agents.policeforce;

import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.intention.SearchPlan;
import implementation.agents.Quadrant;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class CFPoliceForce extends CinvesAgent<PoliceForce> {

    private PolicePlan policePlan;
    private LeaderElectionPlan leaderElectionPlan;

    private RequestReplyPlan requestReplyPlan;

    @Override
    protected void postConnect() {
        super.postConnect();

        policePlan = new PolicePlan(this);
        leaderElectionPlan = new LeaderElectionPlan(this);
        requestReplyPlan = new RequestReplyPlan(this);


        Belief removeBlockades = new Belief();
        removeBlockades.setDataBoolean(true);

        getBeliefs().addBelief(BeliefType.VOLUNTEER,removeBlockades);
        getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());

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
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
