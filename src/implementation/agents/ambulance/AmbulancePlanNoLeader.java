package implementation.agents.ambulance;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.belief.EntityListBelief;
import commlib.cinvesframework.belief.EnvironmentBelief;
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
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.DistanceSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public class AmbulancePlanNoLeader extends AbstractPlan{

    private ArrayList<Integer> rescuedHumans;

    private SearchPlan searchPlan;
    private boolean helping = false;

    public AmbulancePlanNoLeader(CinvesAgent agent){
        super(agent);

        rescuedHumans = new ArrayList<>();
        searchPlan = new SearchPlan(agent);
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
    public Object createPlan(Beliefs beliefs, Desires desires, Intentions intentions) {

        if(desires.getDesire(DesireType.GOAL_LOCATION) == null){
            if(!doAction(beliefs,desires)){
                exploreBuildings(beliefs,desires);
            }
        }else {
            doMove(beliefs,desires);
        }

        return null;
    }


    private boolean doAction(Beliefs beliefs,Desires desires){

        EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);

        ChangeSet changeSet = environmentBelief.getChangeSet();

        EntityID myPosition = ((Human) getAgent().me()).getPosition();

        Collection<Human> humans = GeneralUtils.getHumanTargets(getAgent(),changeSet);

        boolean onRefuge = onRefuge((Human) getAgent().me());

        if(!someoneOnBoard()){
            for (Human human:humans){

                if (rescuedHumans.contains(human.getID().getValue()) || onRefuge || onRefuge(human)){
                    continue;
                }else if(onRoad(human)){
                    int closestRefuge = getClosestRefuge(beliefs,desires,human.getID().getValue());
                    sendRefugeInform(human.getID().getValue(),closestRefuge);
                    continue;
                }

                if(human.getPosition().equals(myPosition)){

                    if(human instanceof Civilian && human.getHP() <= 0){
                        //Esta muerto
                        rescuedHumans.add(human.getID().getValue());//para que ignore el cadaver la proxima vez
                        continue;
                    }

                    if(human instanceof Civilian && human.getBuriedness() == 0){

                        rescuedHumans.add(human.getID().getValue());

                        getAgent().sendLoad(time,human.getID());

                        int closestRefuge = getClosestRefuge(beliefs,desires,getAgent().getID().getValue());
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(closestRefuge)));
                        return true;
                    }

                    if (human instanceof Civilian && human.getBuriedness() > 0){

                        helping = true;
                        getAgent().sendRescue(time,human.getID());

                        return true;
                    }

                }else{
                    if(human instanceof Civilian) {
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(human.getPosition()));
                    }
                    return true;
                }

            }
        }

        return false;

    }

    private void exploreBuildings(Beliefs beliefs,Desires desires){

        EntityListBelief entityListBelief = (EntityListBelief)beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);

        Collections.sort(entityListBelief.getEntities(),new DistanceSorter(getAgent().location(),getAgent().getWorldModel()));

        if(desires.getDesire(DesireType.GOAL_LOCATION) == null){

            StandardEntity entity = entityListBelief.getEntities().get(0);

            entityListBelief.getEntities().remove(0);

            desires.addDesire(DesireType.GOAL_LOCATION,new Desire(entity.getID()));
        }

        doMove(beliefs,desires);

    }

    private void doMove(Beliefs beliefs,Desires desires){


        /**
         * Move
         */

        EntityID myPosition = ((Human) getAgent().me()).getPosition();

        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);

        if (goalLocation.getEntityID().getValue() == myPosition.getValue()) {

            desires.addDesire(DesireType.GOAL_LOCATION,null);

            if(someoneOnBoard()){
                getAgent().sendUnload(time);
                helping = false;
            }

        } else {
            List<EntityID> path = searchPlan.createPlan(beliefs, desires);

            if(path != null) {
                getAgent().sendMove(time, path);
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
            }else{
                System.out.println("Ruta de "+target+" a ->> "+entity.getID()+" fue nula");
            }

        }

        desires.addDesire(DesireType.GOAL_LOCATION, originalGoal);

        return closestRefuge.getID().getValue();
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

    }

    private boolean onRoad(Human human){

        return (human.getPosition(getAgent().getWorldModel()) instanceof Road);

    }
}
