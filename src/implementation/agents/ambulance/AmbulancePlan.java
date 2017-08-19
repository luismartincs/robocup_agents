package implementation.agents.ambulance;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.intention.AbstractPlan;
import commlib.cinvesframework.intention.SearchPlan;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.ActionConstants;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.DistanceSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class AmbulancePlan extends AbstractPlan{

    private SearchPlan searchPlan;
    private int targetBuilding = 0;

    private ArrayList<Integer> rescuedHumans;

    public AmbulancePlan(CinvesAgent agent){
        super(agent);
        searchPlan = new SearchPlan(agent);

        rescuedHumans = new ArrayList<>();
    }

    private void sendRequest(int leader){

        int conversationId = getAgent().nextConversationId();

        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.REQUEST,
                new EntityID(leader),
                conversationId,
                ActionConstants.REQUEST_LOCATION,
                0);

        getAgent().addACLMessageToQueue(conversationId, leaderCFP);
        getAgent().addACLMessage(leaderCFP);

    }

    private void sendInform(int receiver,Beliefs beliefs){

        int conversationId = getAgent().nextConversationId();

        EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);

        EntityID position = biq.getEntities().get(targetBuilding).getID();
        targetBuilding++;

        if(targetBuilding >= biq.getEntities().size()){
            targetBuilding = 0;
        }

        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.INFORM,
                new EntityID(receiver),
                conversationId,
                ActionConstants.REQUEST_LOCATION,
                position.getValue());

        getAgent().addACLMessageToQueue(conversationId, leaderCFP);
        getAgent().addACLMessage(leaderCFP);

    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {

        /**
         * Message control
         */

        stateControl(beliefs,desires);


        /**
         * Actions
         */

        Belief lb = beliefs.getBelief(BeliefType.IM_LEADER);
        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);

        boolean imLeader = lb.isDataBoolean();
        int leaderId = lb.getDataInt();

        if(goalLocation == null){
            if(imLeader){

                EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);

                EntityID position = biq.getEntities().get(targetBuilding).getID();
                targetBuilding++;

                if(targetBuilding >= biq.getEntities().size()){
                    targetBuilding = 0;
                }


                goalLocation = new Desire(position);

                desires.addDesire(DesireType.GOAL_LOCATION, goalLocation);

                doMove(beliefs,desires);

            }else{

                //Solicitarle al lider a donde ir
                sendRequest(leaderId);
            }

        }else{

            doMove(beliefs,desires);

        }

        return null;
    }

    /**
     * ContractNet se revisan los mensajes de solicitud antes de hacer algun movimiento
     */

    private void stateControl(Beliefs beliefs, Desires desires){

        ArrayList<ACLMessage> aclMessages = getAgent().getAclMessages();

        for(ACLMessage msg: aclMessages){

            switch (msg.getPerformative()){
                case REQUEST:

                    if(msg.getContent() == ActionConstants.REQUEST_LOCATION){
                        sendInform(msg.getSender(),beliefs);
                    }

                    break;
                case INFORM:
                    if(msg.getContent() == ActionConstants.REQUEST_LOCATION){
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(0))));
                    }
                    break;
            }

        }
    }


    private int getClosestRefuge(Beliefs beliefs, Desires desires, int target){

        Desire originalGoal = desires.getDesire(DesireType.GOAL_LOCATION);

        EntityListBelief refugesList = (EntityListBelief) beliefs.getBelief(BeliefType.REFUGE);
        ArrayList<StandardEntity> refuges = refugesList.getEntities();

        Human human = (Human) getAgent().getWorldModel().getEntity(new EntityID(target));

        int minSteps = Integer.MAX_VALUE;
        int pathSize = 0;
        StandardEntity closestRefuge = null;
        List<EntityID> path = null;

        for (StandardEntity entity : refuges) {

            Refuge refuge = (Refuge) entity;
            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(refuge.getID()));
            path = searchPlan.createPlan(beliefs, desires,human.getPosition());

            if (path != null) {
                pathSize = path.size();
                if (pathSize < minSteps) {
                    minSteps = pathSize;
                    closestRefuge = refuge;
                }
            }

        }

        desires.addDesire(DesireType.GOAL_LOCATION, originalGoal);

        return closestRefuge.getID().getValue();
    }


    private void doMove(Beliefs beliefs,Desires desires){

        /**
         * Remove Blockade
         */

        EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);


        ChangeSet changeSet = environmentBelief.getChangeSet();

        EntityID myPosition = ((Human) getAgent().me()).getPosition();

        Collection<Human> humans = GeneralUtils.getHumanTargets(getAgent(),changeSet);

        boolean onRefuge = onRefuge((Human) getAgent().me());

        if(!someoneOnBoard()){
            for (Human human:humans){

                if (rescuedHumans.contains(human.getID().getValue()) || onRefuge || onRefuge(human)){
                    continue;
                }

                if(human.getPosition().equals(myPosition)){

                    if(human instanceof Civilian && human.getBuriedness() == 0){
                        rescuedHumans.add(human.getID().getValue());
                        getAgent().sendLoad(time,human.getID());

                        int closestRefuge = getClosestRefuge(beliefs,desires,getAgent().getID().getValue());
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(closestRefuge)));
                        break;
                    }

                    if (human.getBuriedness() > 0){
                        System.out.println("Rescatando");
                        getAgent().sendRescue(time,human.getID());
                        break;
                    }

                }else{
                    desires.addDesire(DesireType.GOAL_LOCATION, new Desire(human.getPosition()));
                }

            }
        }else{
            System.out.println("toy cargando a un civil");
        }




        /**
         * Move
         */

        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);


        if (goalLocation.getEntityID().getValue() == myPosition.getValue()) {

            desires.addDesire(DesireType.GOAL_LOCATION,null);
//            getAgent().sendRest(time);

            if(someoneOnBoard()){
                getAgent().sendUnload(time);
                System.out.println("Dejando al civil");
            }


        } else {
            List<EntityID> path = searchPlan.createPlan(beliefs, desires);
            if(path != null) {
                getAgent().sendMove(time, path);
            }
        }
    }

    private boolean someoneOnBoard() {
        for (StandardEntity next : getAgent().getWorldModel()
                .getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human) next).getPosition().equals(getAgent().getID())) {
                return true;
            }
        }
        return false;
    }

    private boolean onRefuge(Human human){

        return (human.getPosition(getAgent().getWorldModel()) instanceof Refuge);

        /*
        for (StandardEntity r:refugesList.getEntities()){
            if (((Human) getAgent().me()).getPosition().equals(r.getID())) {
                return true;
            }
        }
        return false;*/
    }
}
