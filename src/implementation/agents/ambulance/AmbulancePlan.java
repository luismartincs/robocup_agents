package implementation.agents.ambulance;

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
    private ArrayList<Integer> informedHumans;
    private ArrayList<Integer> reportedBlockades;
    private ArrayList<int[]> nextQuadrantLeaders;
    private int nextQuadrantIndex = 0;

    private int prevPosition = 0;

    private boolean helping = false;

    private int tiempoAtorado = 0;

    private boolean waitingResponse = true;

    public AmbulancePlan(CinvesAgent agent){
        super(agent);
        searchPlan = new SearchPlan(agent);

        rescuedHumans = new ArrayList<>();
        informedHumans = new ArrayList<>();
        reportedBlockades = new ArrayList<>();
        nextQuadrantLeaders = new ArrayList<>();
    }

    private void sendRequest(int leader){

        waitingResponse = true;

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
        //System.out.println("request to ambulance leader");
    }

    private void sendInform(int receiver,Beliefs beliefs,Desires desires,int position){

        int conversationId = getAgent().nextConversationId();

        EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);

        StandardEntity closestBuilding = getClosestBuilding(beliefs,desires,position);

        int actionToInform = ActionConstants.REQUEST_LOCATION;
        int value = 0;

        if(closestBuilding!=null) {
            biq.getEntities().remove(closestBuilding);
            value = closestBuilding.getID().getValue();
        }else{
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

    private void sendReportBlockade(int policeOffice){
        int conversationId = getAgent().nextConversationId();

        Human human = (Human) getAgent().me();

        ACLMessage leaderCFP = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.INFORM,
                new EntityID(policeOffice),
                conversationId,
                ActionConstants.REPORT_BLOCKADE,
                human.getPosition().getValue(),
                getAgent().getCurrentQuadrant());

        getAgent().addACLMessage(leaderCFP);

    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires, Intentions intentions) {

        /**
         * Message control
         */

        Belief lb = beliefs.getBelief(BeliefType.IM_LEADER);
        boolean imLeader = lb.isDataBoolean();

        stateControl(beliefs,desires,imLeader);


        /**
         * Actions
         */


        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);


        int leaderId = lb.getDataInt();

        if(goalLocation == null){

            if(waitingResponse){
                StandardEntity random = getRandomDestination(beliefs,desires);
                desires.addDesire(DesireType.GOAL_LOCATION, new Desire(random.getID()));
                doMove(beliefs,desires);
            }

            if(!doAction(beliefs,desires)){

                if(imLeader){

                    Human human = (Human) getAgent().me();

                    StandardEntity building = getClosestBuilding(beliefs,desires,human.getPosition().getValue());

                    EntityID position = null;

                    if(building != null){

                        EntityListBelief biq = (EntityListBelief)beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);
                        biq.getEntities().remove(building);

                        position = building.getID();

                        goalLocation = new Desire(position);

                        desires.addDesire(DesireType.GOAL_LOCATION, goalLocation);

                        doMove(beliefs,desires);

                    }else {

                        if(nextQuadrantIndex < nextQuadrantLeaders.size()){

                            imLeader = false;

                            int qd[] = nextQuadrantLeaders.get(nextQuadrantIndex);

                            getAgent().setQuadrant(qd[0]);
                            leaderId = qd[1];

                            lb.setDataInt(leaderId);
                            lb.setDataBoolean(false);

                            nextQuadrantIndex++;

                            sendRequest(leaderId);


                        }else{
                            nextQuadrantIndex = 0;
                            int closestRefuge = getClosestRefuge(beliefs,desires,getAgent().getID().getValue());
                            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(closestRefuge)));
                        }

                    }



                }else {
                    sendRequest(leaderId);
                }
            }else {
                if(!helping) { //Si esta ayudando a alguien no solicita una nueva ubicacion hasta que lo cure y lleve al refugio
                    sendRequest(leaderId);
                }
            }

        }else{
            doMove(beliefs,desires);
        }


        return null;
    }


    /**
     *
     */

    private boolean doAction(Beliefs beliefs,Desires desires){

        EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);

        ChangeSet changeSet = environmentBelief.getChangeSet();

        EntityID myPosition = ((Human) getAgent().me()).getPosition();

        Collection<Human> humans = GeneralUtils.getHumanTargets(getAgent(),changeSet);

        boolean onRefuge = onRefuge((Human) getAgent().me());

        if(!someoneOnBoard()){
            for (Human human:humans){

                if (rescuedHumans.contains(human.getID().getValue()) || onRefuge || onRefuge(human) || onRoad(human)){
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
                        waitingResponse = false;
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

                    }
                    break;
            }

        }
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

                EntityListBelief pc = (EntityListBelief)beliefs.getBelief(BeliefType.POLICE_CENTRE);

                PoliceOffice po = (PoliceOffice)pc.getEntities().get(0);

                Human human = (Human)getAgent().me();

                if(prevPosition != human.getPosition().getValue()){
                    tiempoAtorado = 0;
                    prevPosition = human.getPosition().getValue();
                }else{

                    tiempoAtorado++;

                    if(tiempoAtorado > 10){
                        if(!reportedBlockades.contains(prevPosition)){
                            reportedBlockades.add(prevPosition);
                            //System.out.println(prevPosition+" atorado durante "+tiempoAtorado);
                            sendReportBlockade(po.getID().getValue());
                        }
                    }
                }
                getAgent().sendMove(time, path);
            }
        }
    }

    private StandardEntity getRandomDestination(Beliefs beliefs,Desires desires){

        GeneralUtils.updateBuildingsInQuadrant(beliefs,getAgent().getWorldModel(),getAgent().getCurrentQuadrant());


        StandardEntity myPosition = ((Human) getAgent().me()).getPosition(getAgent().getWorldModel());

        EntityListBelief buildingList = (EntityListBelief) beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);
        ArrayList<StandardEntity> buildings = buildingList.getEntities();

        Collections.sort(buildings,new DistanceSorter(myPosition, getAgent().getWorldModel()));

        StandardEntity closestBuilding = null;

        closestBuilding = buildings.get((int)(buildings.size()*Math.random()));

        return closestBuilding;
    }

    /**
     *
     */


    private StandardEntity getClosestBuilding(Beliefs beliefs, Desires desires, int target){

        Desire originalGoal = desires.getDesire(DesireType.GOAL_LOCATION);

        EntityListBelief buildingList = (EntityListBelief) beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT);
        ArrayList<StandardEntity> buildings = buildingList.getEntities();


        int minSteps = Integer.MAX_VALUE;
        int pathSize = 0;
        StandardEntity closestBuilding = null;
        List<EntityID> path = null;

        for (StandardEntity entity : buildings) {

            Building building = (Building) entity;
            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(building.getID()));
            path = searchPlan.createPlan(beliefs, desires,new EntityID(target));

            if (path != null) {
                pathSize = path.size();
                if (pathSize < minSteps) {
                    minSteps = pathSize;
                    closestBuilding = building;
                }
            }

        }

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

    public ArrayList<int[]> getNextQuadrantLeaders() {
        return nextQuadrantLeaders;
    }

    public void setNextQuadrantLeaders(ArrayList<int[]> nextQuadrantLeaders) {
        this.nextQuadrantLeaders = nextQuadrantLeaders;
    }
}
