package implementation.agents.policeforce;

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
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

public class RequestReplyPlan extends AbstractPlan{

    private SearchPlan searchPlan;
    private int targetBuilding = 0;

    public RequestReplyPlan(CinvesAgent agent){
        super(agent);
        searchPlan = new SearchPlan(agent);
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
        //biq.getEntities().remove(targetBuilding);

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

    private void sendRefugeInform(int receiver,int refuge){

        int conversationId = getAgent().nextConversationId();

        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.INFORM,
                new EntityID(receiver),
                conversationId,
                ActionConstants.REQUEST_POLICE_INSTRUCTION,
                refuge);

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
                //biq.getEntities().remove(targetBuilding);
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
                    }else if(msg.getContent() == ActionConstants.REQUEST_POLICE_INSTRUCTION){

                        int closestRefuge = getClosestRefuge(beliefs,desires,msg.getSender());
                        sendRefugeInform(msg.getSender(),closestRefuge);
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
        int distance = ((LocationBelief) beliefs.getBelief(BeliefType.REPAIR_DISTANCE)).getEntityID().getValue();

        Blockade target = GeneralUtils.getTargetBlockade(distance, getAgent());

        if (target != null) {
            getAgent().sendClear(time,target.getID());
        }

        /**
         * Move
         */

        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);
        EntityID myPosition = ((Human) getAgent().me()).getPosition();

        if (goalLocation.getEntityID().getValue() == myPosition.getValue()) {
            desires.addDesire(DesireType.GOAL_LOCATION,null);
            getAgent().sendRest(time);
        } else {
            List<EntityID> path = searchPlan.createPlan(beliefs, desires);
            getAgent().sendMove(time, path);
        }
    }
}
