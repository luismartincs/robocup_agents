package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.policeforce.CFPoliceForce;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

public class GoToRefugePlan extends AbstractPlan {

    private int time;
    private boolean isVolunteer = false;

    public GoToRefugePlan(CinvesAgent agent) {
        super(agent);
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires,Intentions intentions) {

        int distance = ((LocationBelief) beliefs.getBelief(BeliefType.REPAIR_DISTANCE)).getEntityID().getValue();

        isVolunteer = beliefs.getBelief(BeliefType.VOLUNTEER).isDataBoolean();

        Blockade target = GeneralUtils.getTargetBlockade(distance, getAgent());


        if (target != null) {

            if(isVolunteer){ //Si es un voluntario/policia entonces remueve el bloqueo sin reportarlo, pa que

                if(getAgent() instanceof CFPoliceForce){
                    System.out.println("Voy a reportar que ya quite el bloqueo");
                }

                getAgent().sendClear(time,target.getID());

            }else{ //Reporta un bloqueo si no hay alguien que lo este removiendo (policia cerca)

                ChangeSet changeSet = ((EnvironmentBelief)beliefs.getBelief(BeliefType.CHANGED_ENVIRONMENT)).getChangeSet();
                ArrayList<PoliceForce> policeForcesAround = GeneralUtils.getPoliceForceAround(getAgent(),changeSet);

                if (policeForcesAround.size() > 0){

                }else{

                    EntityMapBelief entityMapBelief = (EntityMapBelief)beliefs.getBelief(BeliefType.REPORTED_BLOCKADES);

                    if (!entityMapBelief.contains(target) && time > 5){

                        ACLMessage informBlockade = new ACLMessage(time,getAgent().getID(),ACLPerformative.INFORM,new EntityID(0),getAgent().nextConversationId(),0,target.getX(),target.getY(),0,0,target.getID(),target.getRepairCost());
                        getAgent().addACLMessage(informBlockade);

                        entityMapBelief.addEntity(target);

                    }
                }
            }

            return null;

        } else {
            SearchPlan sp = new SearchPlan(getAgent());

            Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);

            if (goalLocation == null) {

                EntityListBelief refugesList = (EntityListBelief) beliefs.getBelief(BeliefType.REFUGE);
                ArrayList<StandardEntity> refuges = refugesList.getEntities();

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

                desires.addDesire(DesireType.GOAL_LOCATION, new Desire(closestRefuge.getID()));
                beliefs.addBelief(BeliefType.GOAL_PATH, new PathBelief(closestPath));

                getAgent().sendMove(time, path);

                return path;

            } else {

                EntityID position = ((Human) getAgent().me()).getPosition();

                if (goalLocation.getEntityID().getValue() == position.getValue()) {
                    getAgent().sendRest(time);
                    return null;
                } else {

                    List<EntityID> path = sp.createPlan(beliefs, desires);

                    if(path!=null) {
                        getAgent().sendMove(time, path);
                    }
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
