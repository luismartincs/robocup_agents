package implementation.agents.firebrigade;

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
import implementation.agents.config.BeliefsName;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.DistanceSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("Duplicates")
public class FireBrigadePlanNoLeader extends AbstractPlan{

    private SearchPlan searchPlan;

    private int maxWater ;
    private int maxPower ;
    private int maxDistance ;

    public FireBrigadePlanNoLeader(CinvesAgent agent){
        super(agent);

        searchPlan = new SearchPlan(agent);

        maxWater = ((CFFireBrigade)agent).getMaxWater();
        maxPower = ((CFFireBrigade)agent).getMaxPower();
        maxDistance = ((CFFireBrigade)agent).getMaxDistance();

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


    private void sendReportFire(int fireLocation,int fireIny){

        int conversationId = getAgent().nextConversationId();

        ACLMessage inform = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.INFORM,
                new EntityID(0),
                conversationId,
                ActionConstants.INFORM_FIRE,
                getAgent().getCurrentQuadrant(),
                fireLocation,
                fireIny);

        getAgent().addACLMessage(inform);
    }


    @Override
    public Object createPlan(Beliefs beliefs, Desires desires, Intentions intentions) {

        stateControl(beliefs,desires,false);

        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);

        if(goalLocation == null){

            if(!doAction(beliefs,desires,intentions)){


                    GeneralUtils.updateRoadsInQuadrant(beliefs,getAgent().getWorldModel(),getAgent().getCurrentQuadrant());

                    Human human = (Human) getAgent().me();

                    StandardEntity building = getRandomDestination(beliefs);

                    EntityID position = null;

                    if(building != null){

                        EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.ROADS_IN_QUADRANT);
                        biq.getEntities().remove(building);

                        position = building.getID();

                        goalLocation = new Desire(position);

                        desires.addDesire(DesireType.GOAL_LOCATION, goalLocation);

                        doMove(beliefs,desires,intentions);

                    }

            }else {

                boolean reloading = (boolean) intentions.getIntention(CFFireBrigade.RELOAD);
                boolean returnToGoal = (boolean) intentions.getIntention(CFFireBrigade.RETURN);

                if(!reloading) {
                    if(!returnToGoal) {

                        GeneralUtils.updateRoadsInQuadrant(beliefs,getAgent().getWorldModel(),getAgent().getCurrentQuadrant());

                        Human human = (Human) getAgent().me();

                        StandardEntity building = getRandomDestination(beliefs);

                        EntityID position = null;

                        if(building != null){

                            EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.ROADS_IN_QUADRANT);
                            biq.getEntities().remove(building);

                            position = building.getID();

                            goalLocation = new Desire(position);

                            desires.addDesire(DesireType.GOAL_LOCATION, goalLocation);

                            doMove(beliefs,desires,intentions);

                        }

                    }else {
                        doMove(beliefs,desires,intentions);
                    }
                }
            }

        }else{

            doMove(beliefs,desires,intentions);
        }


        return null;
    }

    private void stateControl(Beliefs beliefs, Desires desires,boolean imLeader){

        ArrayList<ACLMessage> aclMessages = getAgent().getAclMessages();

        for(ACLMessage msg: aclMessages){

            switch (msg.getPerformative()){
                case REQUEST:

                    if(msg.getContent() == ActionConstants.REQUEST_POLICE_INSTRUCTION){
                        int closestRefuge = getClosestRefuge(beliefs,desires,msg.getSender());
                        sendRefugeInform(msg.getSender(),closestRefuge);
                    }

                    break;
                case INFORM:

                    if(msg.getContent() == ActionConstants.REQUEST_LOCATION){
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(0))));
                    }else if(msg.getContent() == ActionConstants.CHANGE_QUADRANT){


                    }else if(msg.getContent() == ActionConstants.INFORM_FIRE && msg.getExtra(0) == getAgent().getCurrentQuadrant()){

                        if(msg.getExtra(2) > (int)beliefs.getBelief(CFFireBrigade.FIRE_INY)){ //Si esta mas denso que el que apagaba
                            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(1))));
                        }

                    }else if(msg.getContent() == ActionConstants.INFORM_FIRE){ //Si soy de otro cuadrante y no tengo nada que hacer
                        if((int)beliefs.getBelief(CFFireBrigade.FIRE_INY) == 0 && msg.getExtra(2) > 1){
                            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(1))));
                        }
                    }

                    break;
            }

        }
    }


    /**
     *
     *
     * */

    private boolean doAction(Beliefs beliefs,Desires desires,Intentions intentions){

        EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);

        ChangeSet changeSet = environmentBelief.getChangeSet();

        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());

        ArrayList<Building> targetBuildings = GeneralUtils.getBurningBuildings(getAgent(),changeSet);
        sortByFieryness(targetBuildings);

        FireBrigade fireBrigade = (FireBrigade)getAgent().me();


        boolean reloading = (boolean) intentions.getIntention(CFFireBrigade.RELOAD);
        boolean hasWater = (boolean) beliefs.getBelief(CFFireBrigade.HAS_WATER);
        int currentWater = fireBrigade.getWater();
        int requiredWater = (Integer)beliefs.getBelief(BeliefsName.REQUIRED_WATER);


        if(!hasWater && (myPosition instanceof Hydrant)){

            if(currentWater >= maxWater){

                System.out.println("Full, regresando");

                intentions.addIntention(CFFireBrigade.RELOAD,false);
                intentions.addIntention(CFFireBrigade.RETURN,true);

                beliefs.addBelief(CFFireBrigade.HAS_WATER,true);
                desires.addDesire(DesireType.GOAL_LOCATION, (Desire) intentions.getIntention(CFFireBrigade.OLD_GOAL));

            }else {
                intentions.addIntention(CFFireBrigade.RELOAD,true);
                getAgent().sendRest(time);
                //System.out.println("Reloading..."+currentWater);

            }

            return true;
        }

        /**
         * Tengo suficiente agua?
         */

        if(currentWater < requiredWater)
        {
            System.out.println("Este bombero necesita recargar Agua.");

            ArrayList<StandardEntity> hydrants = (ArrayList<StandardEntity>) beliefs.getBelief(BeliefsName.HYDRANTS);

            Collections.sort(hydrants, new DistanceSorter(myPosition, getAgent().getWorldModel()));

            Desire oldGoal = new Desire(myPosition.getID());

            intentions.addIntention(CFFireBrigade.OLD_GOAL,oldGoal);

            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(hydrants.get(0).getID()));

            beliefs.addBelief(CFFireBrigade.HAS_WATER,false);

            return true;
        }

        /**
         * Checo los edificios en llamas e intento apagar el que este mas incendiado
         */

        for (Building building:targetBuildings){
            if (getAgent().getWorldModel().getDistance(getAgent().getID(), building.getID()) <= maxDistance) {

                beliefs.addBelief(CFFireBrigade.FIRE_INY,building.getFieryness()); //Se guarda la intensidad del fuego que estas apagando

                ArrayList<Integer> reportedBuildings = (ArrayList<Integer>) beliefs.getBelief(CFFireBrigade.REPORTED_FIRE);

                if(!reportedBuildings.contains(building.getID().getValue())) {
                    //reportedBuildings.add(building.getID().getValue());
                    sendReportFire(myPosition.getID().getValue(), building.getFieryness());
                }

                getAgent().sendExtinguish(time, building.getID(), maxPower);

                System.out.println("Agua: "+fireBrigade.getWater()+" Building "+building.getFieryness());

                return true;
            }
        }

        return false;

    }

    private void doMove(Beliefs beliefs,Desires desires, Intentions intentions){

        /**
         * Move
         */

        boolean hasWater = (boolean) beliefs.getBelief(CFFireBrigade.HAS_WATER);

        FireBrigade fireBrigade = (FireBrigade)getAgent().me();
        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());

        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);
        int currentWater = fireBrigade.getWater();
        int requiredWater = (Integer)beliefs.getBelief(BeliefsName.REQUIRED_WATER);

        if(hasWater){

            if(currentWater < requiredWater)
            {
                System.out.println("Este bombero necesita recargar Agua.");

                ArrayList<StandardEntity> hydrants = (ArrayList<StandardEntity>) beliefs.getBelief(BeliefsName.HYDRANTS);

                Collections.sort(hydrants, new DistanceSorter(myPosition, getAgent().getWorldModel()));

                Desire oldGoal = new Desire(myPosition.getID());

                intentions.addIntention(CFFireBrigade.OLD_GOAL,oldGoal);

                desires.addDesire(DesireType.GOAL_LOCATION, new Desire(hydrants.get(0).getID()));

                beliefs.addBelief(CFFireBrigade.HAS_WATER,false);

                return;
            }

            EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);

            ChangeSet changeSet = environmentBelief.getChangeSet();
            ArrayList<Building> targetBuildings = GeneralUtils.getBurningBuildings(getAgent(),changeSet);
            sortByFieryness(targetBuildings);

            int lastFireIny = (int) beliefs.getBelief(CFFireBrigade.FIRE_INY);

            for (Building building:targetBuildings){
                if (getAgent().getWorldModel().getDistance(getAgent().getID(), building.getID()) <= maxDistance && (building.getFieryness() >= lastFireIny)) {

                    beliefs.addBelief(CFFireBrigade.FIRE_INY,building.getFieryness());

                    ArrayList<Integer> reportedBuildings = (ArrayList<Integer>) beliefs.getBelief(CFFireBrigade.REPORTED_FIRE);

                    if(!reportedBuildings.contains(building.getID().getValue())) {
                        //reportedBuildings.add(building.getID().getValue());
                        sendReportFire(myPosition.getID().getValue(), building.getFieryness());
                    }

                    getAgent().sendExtinguish(time, building.getID(), maxPower);

                    System.out.println("Agua: "+fireBrigade.getWater()+" Building "+building.getFieryness()+" "+building.getTemperature()+" "+building.getImportance());
                    return;
                }
            }

        }

        int distance = ((LocationBelief) beliefs.getBelief(BeliefType.REPAIR_DISTANCE)).getEntityID().getValue();

        Blockade target = GeneralUtils.getTargetBlockade(distance, getAgent());

        if (target != null) {
            getAgent().sendClear(time,target.getID());
        }


        boolean returnToGoal = (boolean) intentions.getIntention(CFFireBrigade.RETURN);

        if (goalLocation.getEntityID().getValue() == myPosition.getID().getValue()) {

            if(returnToGoal){
                intentions.addIntention(CFFireBrigade.RETURN,false);
            }
            desires.addDesire(DesireType.GOAL_LOCATION,null);

        } else {
            List<EntityID> path = searchPlan.createPlan(beliefs, desires);
            if(path != null) {
                getAgent().sendMove(time, path);
            }
        }

    }


    private StandardEntity getRandomDestination(Beliefs beliefs){


        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());

        EntityListBelief buildingList = (EntityListBelief) beliefs.getBelief(BeliefType.ROADS_IN_QUADRANT);
        ArrayList<StandardEntity> buildings = buildingList.getEntities();

        Collections.sort(buildings,new DistanceSorter(myPosition, getAgent().getWorldModel()));

        StandardEntity closestBuilding = null;

        closestBuilding = buildings.get((int)(buildings.size()*Math.random()));


        return closestBuilding;
    }


    private int getClosestRefuge(Beliefs beliefs, Desires desires, int target){

        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());

        EntityListBelief refugesList = (EntityListBelief) beliefs.getBelief(BeliefType.REFUGE);
        ArrayList<StandardEntity> refuges = refugesList.getEntities();

        Collections.sort(refuges,new DistanceSorter(myPosition, getAgent().getWorldModel()));

        StandardEntity closestRefuge = refuges.get(0);

        return closestRefuge.getID().getValue();
    }


    private void sortByFieryness(ArrayList<Building> buildings){
        Collections.sort(buildings, new Comparator<Building>() {
            @Override
            public int compare(Building o1, Building o2) {
                return o2.getTemperature() - o1.getTemperature();
            }
        });
    }

}
