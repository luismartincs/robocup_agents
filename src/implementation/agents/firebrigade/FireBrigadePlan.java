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
import implementation.agents.firebrigade.CFFireBrigade;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.DistanceSorter;

import java.util.*;

@SuppressWarnings("Duplicates")
public class FireBrigadePlan extends AbstractPlan{

    private SearchPlan searchPlan;
    private int targetBuilding = 0;

    private ArrayList<Integer> extinguishedFires;
    private ArrayList<Integer> informedFires;


    private ArrayList<int[]> nextQuadrantLeaders;
    private int nextQuadrantIndex = 0;

    private boolean helping = false;
    private int maxWater ;
    private int maxPower ;
    private int maxDistance ;

    private boolean bIsExtinguishing;

    public FireBrigadePlan(CinvesAgent agent){
        super(agent);
        searchPlan = new SearchPlan(agent);

        maxWater = ((CFFireBrigade)agent).getMaxWater();
        maxPower = ((CFFireBrigade)agent).getMaxPower();
        maxDistance = ((CFFireBrigade)agent).getMaxDistance();
        //System.out.println("los max Water, power y distances de los bomberos son: " + maxWater + ", " + maxPower + ", " +  maxDistance );
        nextQuadrantLeaders = new ArrayList<>();
        extinguishedFires = new ArrayList<>();
        informedFires = new ArrayList<>();
        targetBuilding = -1;
        bIsExtinguishing = false;
    }

    private void sendRequest(int leader){

        int conversationId = getAgent().nextConversationId();

        Human human = (Human) getAgent().me();


        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.REQUEST,
                new EntityID(leader),
                conversationId,
                ActionConstants.REQUEST_LOCATION,
                human.getPosition().getValue(),
                getAgent().getCurrentQuadrant());

        getAgent().addACLMessageToQueue(conversationId, leaderCFP);
        getAgent().addACLMessage(leaderCFP);

    }

    private void sendInform(int receiver,Beliefs beliefs,Desires desires,int position){

        GeneralUtils.updateRoadsInQuadrant(beliefs,getAgent().getWorldModel(),getAgent().getCurrentQuadrant());

        int conversationId = getAgent().nextConversationId();

        EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.ROADS_IN_QUADRANT);

        StandardEntity closestBuilding = getClosestBuilding(beliefs,desires,position);

        int actionToInform = ActionConstants.REQUEST_LOCATION;
        int value = 0;

        if(closestBuilding!=null) {

            biq.getEntities().remove(closestBuilding);
            value = closestBuilding.getID().getValue();

        }else{ //Aqui ya no deberia entrar, ya que se actualiza la lista al estar vacia
            actionToInform = ActionConstants.CHANGE_QUADRANT;
        }

        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.INFORM,
                new EntityID(receiver),
                conversationId,
                actionToInform,
                value);

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

        /**
         * Message control
         */
        //System.out.println("CFFireBrigade creating a plan");

        Belief lb = beliefs.getBelief(BeliefType.IM_LEADER);
        boolean imLeader = lb.isDataBoolean();

        stateControl(beliefs,desires,imLeader);


        /**
         * Actions
         */


        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);


        int leaderId = lb.getDataInt();


        if(goalLocation == null){

            if(!doAction(beliefs,desires,intentions)){

                if(imLeader){

                    GeneralUtils.updateRoadsInQuadrant(beliefs,getAgent().getWorldModel(),getAgent().getCurrentQuadrant());

                    Human human = (Human) getAgent().me();

                    StandardEntity building = getClosestBuilding(beliefs,desires,human.getPosition().getValue());

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
                    sendRequest(leaderId);
                }

            }else {

                boolean reloading = (boolean) intentions.getIntention(CFFireBrigade.RELOAD);
                boolean returnToGoal = (boolean) intentions.getIntention(CFFireBrigade.RETURN);

                if(!reloading) {
                    if(!returnToGoal) {
                        sendRequest(leaderId);
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


    /**
     *
     */

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




    /**
     * ContractNet se revisan los mensajes de solicitud antes de hacer algun movimiento
     */

    private void stateControl(Beliefs beliefs, Desires desires,boolean imLeader){

        ArrayList<ACLMessage> aclMessages = getAgent().getAclMessages();

        for(ACLMessage msg: aclMessages){

            switch (msg.getPerformative()){
                case REQUEST:

                    if(msg.getContent() == ActionConstants.REQUEST_LOCATION && imLeader && getAgent().getCurrentQuadrant() == msg.getExtra(1)){
                        sendInform(msg.getSender(),beliefs,desires,msg.getExtra(0));

                    }else if(msg.getContent() == ActionConstants.REQUEST_POLICE_INSTRUCTION){
                        int closestRefuge = getClosestRefuge(beliefs,desires,msg.getSender());
                        sendRefugeInform(msg.getSender(),closestRefuge);
                    }
                    break;
                case INFORM:

                    if(msg.getContent() == ActionConstants.REQUEST_LOCATION){
                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(0))));
                    }else if(msg.getContent() == ActionConstants.CHANGE_QUADRANT){

                        if(nextQuadrantIndex < nextQuadrantLeaders.size()){

                            int qd[] = nextQuadrantLeaders.get(nextQuadrantIndex);

                            Belief lb = beliefs.getBelief(BeliefType.IM_LEADER);

                            getAgent().setQuadrant(qd[0]);
                            lb.setDataInt(qd[1]);
                            lb.setDataBoolean(false);

                            nextQuadrantIndex++;

                            sendRequest(qd[1]);

                        }else{
                            nextQuadrantIndex = 0;
                            int closestRefuge = getClosestRefuge(beliefs,desires,getAgent().getID().getValue());
                            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(closestRefuge)));
                        }

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
     * Se usa para revisar si un edificio acaba de apagarse, y entonces agregarlo en la lista de fuegos extinguidos.
     * @param buildingID
     * @param targetBuildings
     * @return
     */
    private boolean checkExtinguished(int buildingID, Collection<Building> targetBuildings )
    {
        for (Building building:targetBuildings){
            if(building.getID().getValue() == buildingID)
            {
                //System.out.println("Checking extinguished is FALSE para el bombero: " + getAgent().getID());
                return false;
            }
        }
        /*Entonces ya se extinguió*/
        extinguishedFires.add(buildingID);
        return true;
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


    /**
     *
     */


    private StandardEntity getClosestBuilding(Beliefs beliefs, Desires desires, int target){

        Desire originalGoal = desires.getDesire(DesireType.GOAL_LOCATION);

        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());

        EntityListBelief buildingList = (EntityListBelief) beliefs.getBelief(BeliefType.ROADS_IN_QUADRANT);
        ArrayList<StandardEntity> buildings = buildingList.getEntities();

        Collections.sort(buildings,new DistanceSorter(myPosition, getAgent().getWorldModel()));


        int minSteps = 0;
        int pathSize = 0;
        StandardEntity closestBuilding = null;
        List<EntityID> path = null;

        closestBuilding = buildings.get((int)(buildings.size()*Math.random()));

        desires.addDesire(DesireType.GOAL_LOCATION, originalGoal);

        return closestBuilding;
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


    private void sortByFieryness(ArrayList<Building> buildings){
        Collections.sort(buildings, new Comparator<Building>() {
            @Override
            public int compare(Building o1, Building o2) {
                return o2.getTemperature() - o1.getTemperature();
            }
        });
    }

    public ArrayList<int[]> getNextQuadrantLeaders() {
        return nextQuadrantLeaders;
    }

    public void setNextQuadrantLeaders(ArrayList<int[]> nextQuadrantLeaders) {
        this.nextQuadrantLeaders = nextQuadrantLeaders;
    }
}
