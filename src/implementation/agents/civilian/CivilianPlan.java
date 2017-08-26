package implementation.agents.civilian;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.intention.AbstractPlan;
import commlib.cinvesframework.intention.Intentions;
import commlib.cinvesframework.intention.SearchPlan;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.ActionConstants;
import implementation.agents.ambulance.AmbulancePlan;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.DistanceSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CivilianPlan extends AbstractPlan{

    private SearchPlan searchPlan;
    private boolean isVolunteer;
    private boolean goingToRefuge = false;
    private boolean onRefuge = false;

    public CivilianPlan(CinvesAgent agent){
        super(agent);

        searchPlan = new SearchPlan(agent);
    }


    private void sendRequest(int receiver){
        int conversationId = getAgent().nextConversationId();

        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.REQUEST,
                new EntityID(receiver),
                conversationId,
                ActionConstants.REQUEST_POLICE_INSTRUCTION,
                0);

        getAgent().addACLMessageToQueue(conversationId, leaderCFP);
        getAgent().addACLMessage(leaderCFP);

    }


    @Override
    public List<EntityID> createPlan(Beliefs beliefs, Desires desires, Intentions intentions) {

        stateControl(beliefs,desires);


        isVolunteer = beliefs.getBelief(BeliefType.VOLUNTEER).isDataBoolean();
        onRefuge = onRefuge((Human)getAgent().me());

        if(!onRefuge){
            if(isVolunteer){

                if(!goingToRefuge) {
                    policeAround(beliefs, desires);
                }

                removeBlockades(beliefs,desires);

                List<EntityID> steps = randomDestination(beliefs,desires);

                if(steps != null){
                    getAgent().sendMove(time,steps);
                }else {
                   // System.out.println("El civil "+getAgent().getID()+" v");
                }

            }else{

                if(!goingToRefuge) {
                    policeAround(beliefs, desires);
                }

                List<EntityID> steps = randomDestination(beliefs,desires);

                if(steps != null){
                    getAgent().sendMove(time,steps);
                }else {
                    //System.out.println("El civil "+getAgent().getID()+" no volunteer");
                }



            }
        }else{
            getAgent().sendRest(time);
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
                case INFORM:
                    if(msg.getContent() == ActionConstants.REQUEST_POLICE_INSTRUCTION){
                        goingToRefuge = true;
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(0))));
                    }
                    break;
            }

        }
    }





    private void policeAround(Beliefs beliefs, Desires desires){

        EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);

        ChangeSet changeSet = environmentBelief.getChangeSet();

        ArrayList<Human> humans = GeneralUtils.getHumanTargets(getAgent(),changeSet);
        Human yoMerengues = (Human)getAgent().me();

        for (Human human:humans) {

            if(onRoad(yoMerengues)){
                if(human instanceof PoliceForce || human instanceof AmbulanceTeam || human instanceof FireBrigade){
                    sendRequest(human.getID().getValue());
                    return;
                }
            }

        }

    }

    private void removeBlockades(Beliefs beliefs, Desires desires){

        int distance = ((LocationBelief) beliefs.getBelief(BeliefType.REPAIR_DISTANCE)).getEntityID().getValue();

        Blockade target = GeneralUtils.getTargetBlockade(distance, getAgent());

        if (target != null) {
            getAgent().sendClear(time,target.getID());
        }
    }

    private List<EntityID> randomDestination(Beliefs beliefs, Desires desires){

        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());
        EntityListBelief buildings = (EntityListBelief)beliefs.getBelief(BeliefType.ROADS);
        ArrayList<StandardEntity> buildingIDs = buildings.getEntities();

        Collections.sort(buildingIDs, new DistanceSorter(myPosition, getAgent().getWorldModel()));

        Desire targetBuilding = desires.getDesire(DesireType.GOAL_LOCATION);
        EntityID target = null;

        if(targetBuilding == null){
            target = buildingIDs.get(buildingIDs.size()-1).getID();//buildingIDs.get((int)(Math.random()*buildingIDs.size())).getID();
        }else{
            target = targetBuilding.getEntityID();
        }

        List<EntityID> targets = new ArrayList<>();

        targets.add(target);

        desires.addDesire(DesireType.GOAL_LOCATION,new Desire(target));

        List<EntityID> path = searchPlan.createPlan(beliefs,desires);

        return path;
    }

    private boolean onRefuge(Human human){

        return (human.getPosition(getAgent().getWorldModel()) instanceof Refuge);

    }

    private boolean onRoad(Human human){

        //System.out.println("Civil "+human.getID()+" "+human.getPosition(getAgent().getWorldModel()).getClass());

        return (human.getPosition(getAgent().getWorldModel()) instanceof Road);

    }

}
