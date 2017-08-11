package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.utils.GeneralUtils;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

public class GoToRefugePlan extends AbstractPlan {

    private int time;
    private boolean removeBlockades = false;

    public GoToRefugePlan(CinvesAgent agent) {
        super(agent);
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {

        int distance = ((LocationBelief) beliefs.getBelief(BeliefType.REPAIR_DISTANCE)).getEntityID().getValue();

        Blockade target = null;

        if(removeBlockades){
            target = GeneralUtils.getTargetBlockade(distance, getAgent());
        }

        if (target != null) {
            getAgent().onBlockadeDetected(time,target.getID());
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
                    getAgent().sendMove(time, path);
                    return path;
                }
            }
        }
    }

    public boolean isRemoveBlockades() {
        return removeBlockades;
    }

    public void setRemoveBlockades(boolean removeBlockades) {
        this.removeBlockades = removeBlockades;
    }
}
