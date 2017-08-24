package implementation.agents.firebrigade;

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
import implementation.agents.firebrigade.CFFireBrigade;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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


    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {

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

            if(!doAction(beliefs,desires)){

                if(imLeader){
                    //System.out.println("Lider bombero activo.");

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

        Collection<Building> targetBuildings = GeneralUtils.getBurningBuildings(getAgent(),changeSet);

        boolean onRefuge = onRefuge((Human) getAgent().me());

        int tmpWaterLevel = ((FireBrigade)(getAgent().me())).getWater(); //Cast it to FireBrigade, so we can access the getWater method.


        // Are we currently filling with water?
        if( onRefuge && tmpWaterLevel < maxWater )
        {
            System.out.println("El Bombero: " + getAgent().getID() + " está Cargando agua en: " + myPosition);
            getAgent().sendRest(time);//rest while it charges water.
            return true;
        }

        // Are we out of water?
        if(tmpWaterLevel < 10 /*water threshold*/)
        {
            System.out.println("Este bombero necesita recargar Agua.");
            // Head for a refuge
            //then we charge the water tank.
            int closestRefuge = getClosestRefuge(beliefs,desires,getAgent().getID().getValue());
            desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(closestRefuge)));
            targetBuilding = -1;
            return true;
        }

        //revisamos si ya se apagó el fuego que queríamos apagar.
        if(checkExtinguished(targetBuilding, targetBuildings) && targetBuilding != -1)
        {
            //System.out.println("El bombero " + getAgent().getID() + " ya extinguió su fuego objetivo. Puede tener otro distinto.");
            targetBuilding = -1;//Si, sí, entonces ya es posible asignar uno nuevo a extinguir.
        }
        //if we have enough water,
        // Find all buildings that are on fire
        for (Building building:targetBuildings){
            if (extinguishedFires.contains(building.getID().getValue()) ){
                if(!building.isOnFire()){
                    //System.out.println("El bombero " + getAgent().getID() + " ya extinguió el fuego del edificio: "  + building.getID().getValue());
                    continue;
                }
            }

            int DistanceToFire = getAgent().getWorldModel().getDistance(getAgent().getID(), building.getID());
            //System.out.println("El Bombero: " + getAgent().getID() + " está a esta distnacia de su objetivo_:  " + DistanceToFire);
            if (DistanceToFire <= maxDistance ) {

                //System.out.println("El Bombero: " + getAgent().getID() + " está en distancia para exitinguir.");
                if(targetBuilding == -1)
                {
                    System.out.println("El bombero " + getAgent().getID() + " ya tiene un nuevo edificio objetivo: " + building.getID().getValue() );
                    targetBuilding = building.getID().getValue();
                }
                else if(targetBuilding != building.getID().getValue())
                {
                   continue;//para que solamente el targetbuilding entre al extinguish.
                }
                System.out.println("El bombero " + getAgent().getID() +" está Extinguiendo el edificio: " + building.getID());
                getAgent().sendExtinguish(time, building.getID(), maxPower);
                //sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
                return true;
            }
            else{
                /**DUDA!!!!!!!!!*/
                //System.out.println("el bombero " + getAgent().getID() + " tiene como objetivo: " + building.getID());
                //new EntityID( (int)building.getLocation(getAgent().getWorldModel()) )
                desires.addDesire(DesireType.GOAL_LOCATION, new Desire( new EntityID(building.getID().getValue())/*es la posición?*/ ));
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

                    }else {
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


    private void doMove(Beliefs beliefs,Desires desires){


        /**
         * Move
         */
        Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);
        StandardEntity tmpSE = getAgent().getWorldModel().getEntity(goalLocation.getEntityID());
        if( ! tmpSE.getStandardURN().equals(StandardEntityURN.REFUGE)) //Si no se dirige a un refugio.
        {
            //System.out.println("Entró el Bombero: " + getAgent().getID() + " a un fuego más cercano que su objetivo actual." );
            EnvironmentBelief environmentBelief = (EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT);

            ChangeSet changeSet = environmentBelief.getChangeSet();
            ArrayList<Building> targetBuildings = GeneralUtils.getBurningBuildings(getAgent(),changeSet);

            if(!targetBuildings.isEmpty() && targetBuildings.get(0).getID().getValue() != goalLocation.getEntityID().getValue())
            {
                //Then this is closer than the other goal., so we change the goal.
                desires.addDesire(DesireType.GOAL_LOCATION,new Desire( new EntityID(targetBuildings.get(0).getID().getValue() ) ) );
                goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);
            }
        }
        else //entonces sí se dirige a un refugio.
        {
            //System.out.println("El Bombero: " + getAgent().getID() + " se dirige a un refugio.");
        }

        EntityID myPosition = ((Human) getAgent().me()).getPosition();

        if (goalLocation.getEntityID().getValue() == myPosition.getValue()) {

            desires.addDesire(DesireType.GOAL_LOCATION,null);

            //System.out.println("El Bombero: " + getAgent().getID() + " YA llegó a su GOAL_LOCATION"  );
            /*if(someoneOnBoard()){
                getAgent().sendUnload(time);
                helping = false;
            } Creo que ya no es necesario. */


        } else {
            List<EntityID> path = searchPlan.createPlan(beliefs, desires);
            if(path != null) {
                //System.out.println("El Bombero: " + getAgent().getID() + " está caminando hacia su Goal_location.");
                getAgent().sendMove(time, path);
            }
        }
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
            }

        }

        desires.addDesire(DesireType.GOAL_LOCATION, originalGoal);

        return closestRefuge.getID().getValue();
    }


    private boolean onRefuge(Human human){

        return (human.getPosition(getAgent().getWorldModel()) instanceof Refuge);

    }

    public ArrayList<int[]> getNextQuadrantLeaders() {
        return nextQuadrantLeaders;
    }

    public void setNextQuadrantLeaders(ArrayList<int[]> nextQuadrantLeaders) {
        this.nextQuadrantLeaders = nextQuadrantLeaders;
    }
}
