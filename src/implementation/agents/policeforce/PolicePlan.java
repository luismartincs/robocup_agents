package implementation.agents.policeforce;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.*;
import commlib.cinvesframework.intention.AbstractPlan;
import commlib.cinvesframework.intention.SearchPlan;
import commlib.cinvesframework.interaction.ContractNet;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.ActionConstants;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;


public class PolicePlan extends AbstractPlan {

    private int time;
    private boolean isVolunteer = false;

    public PolicePlan(CinvesAgent agent) {
        super(agent);
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {

        SearchPlan sp = new SearchPlan(getAgent());

        /**
         * ContractNet se revisan los mensajes de solicitud antes de hacer algun movimiento
         */
        ArrayList<ACLMessage> aclMessages = getAgent().getAclMessages();

        for(ACLMessage msg: aclMessages){

            ACLMessage previous = getAgent().getACLMessageFromQueue(msg.getConversationId());

            if(previous != null) {

                if (ContractNet.isValidState(previous.getPerformative(), msg.getPerformative())) {
                    System.out.println(msg.getPerformative() + " - " + msg.getBlockade());

                    desires.addDesire(DesireType.GOAL_LOCATION, new Desire(msg.getBlockade()));
                    getAgent().removeACLMessageFromQueue(msg.getConversationId());
                }

            }

        }

        /**
         * Comportamiento normal
         */


        int distance = ((LocationBelief) beliefs.getBelief(BeliefType.REPAIR_DISTANCE)).getEntityID().getValue();

        Blockade target = GeneralUtils.getTargetBlockade(distance, getAgent());


        if (target != null) {



            EntityMapBelief entityMapBelief = (EntityMapBelief)beliefs.getBelief(BeliefType.REPORTED_BLOCKADES);

            if (!entityMapBelief.contains(target) && time > 5){

               // System.out.println("Voy a reportar que ya quite el bloqueo");
                /*
                ACLMessage informBlockade = new ACLMessage(time,getAgent().getID(), ACLPerformative.INFORM,new EntityID(0),getAgent().nextConversationId(),0,target.getX(),target.getY(),0,0,target.getID(),target.getRepairCost());
                getAgent().addACLMessage(informBlockade);
                */
                entityMapBelief.addEntity(target);

            }

            getAgent().sendClear(time,target.getID());

            return null;

        } else {



            Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);

            if (goalLocation == null) {

                EntityListBelief refugesList = (EntityListBelief) beliefs.getBelief(BeliefType.REFUGE);
                ArrayList<StandardEntity> refuges = refugesList.getEntities();

                EntityListDesire nextGoals = new EntityListDesire();


                int minSteps = Integer.MAX_VALUE;
                int pathSize = 0;
                StandardEntity closestRefuge = null;
                List<EntityID> closestPath = null;
                List<EntityID> path = null;

                for (StandardEntity entity : refuges) {

                    Refuge refuge = (Refuge) entity;
                    desires.addDesire(DesireType.GOAL_LOCATION, new Desire(refuge.getID()));
                    path = sp.createPlan(beliefs, desires);

                    if (path != null) {

                        pathSize = path.size();

                        if (pathSize < minSteps) {
                            minSteps = pathSize;
                            closestRefuge = refuge;
                            closestPath = path;
                        }

                    }

                }


                for (StandardEntity ref:refuges) {
                    if(ref.getID().getValue() != closestRefuge.getID().getValue()) {
                        nextGoals.addEntity(ref);
                    }
                }

                desires.addDesire(DesireType.NEXT_GOALS,nextGoals);

                desires.addDesire(DesireType.GOAL_LOCATION, new Desire(closestRefuge.getID()));
                beliefs.addBelief(BeliefType.GOAL_PATH, new PathBelief(closestPath));

                getAgent().sendMove(time, path);

                return path;

            } else {

                EntityID position = ((Human) getAgent().me()).getPosition();

                if (goalLocation.getEntityID().getValue() == position.getValue()) {

                    EntityListDesire nextGoals = (EntityListDesire)desires.getDesire(DesireType.NEXT_GOALS);

                    if(nextGoals.getEntities().size() > 0){

                        StandardEntity entity = nextGoals.getEntities().get(0);

                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(entity.getID()));

                        List<EntityID> path = sp.createPlan(beliefs, desires);

                        nextGoals.removeEntity(entity);

                        getAgent().sendMove(time, path);

                    }else {


                        if(getAgent().getQueuedMessages().size() <= 0){

                            //System.out.println("SOLICITAR DONDE QUITAR BLOQUEO");

                            int conversationId = getAgent().nextConversationId();

                            ACLMessage requestBlockade = new ACLMessage(time,getAgent().getID(), ACLPerformative.REQUEST,new EntityID(0),
                                    conversationId, ActionConstants.REQUEST_BLOCKADE,0,0,0,0,new EntityID(0),0);

                            getAgent().addACLMessageToQueue(conversationId,requestBlockade);

                            getAgent().addACLMessage(requestBlockade);
                        }

                        getAgent().sendRest(time);
                    }

                    return null;
                } else {

                    List<EntityID> path = sp.createPlan(beliefs, desires);
                    getAgent().sendMove(time, path);
                    return path;
                }
            }
        }
    }

    public boolean isVolunteer() {
        return isVolunteer;
    }

    public void setVolunteer(boolean volunteer) {
        this.isVolunteer = volunteer;
    }
}

