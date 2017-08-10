package implementation.agents.civilian;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.belief.EntityListBelief;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.intention.AbstractPlan;
import commlib.cinvesframework.intention.SearchPlan;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

public class CivilianPlan extends AbstractPlan{

    private SearchPlan searchPlan;

    public CivilianPlan(CinvesAgent agent){
        super(agent);

        searchPlan = new SearchPlan(agent);
    }

    @Override
    public List<EntityID> createPlan(Beliefs beliefs, Desires desires) {

        List<EntityID> steps = randomDestination(beliefs,desires);

        return steps;
    }

    private List<EntityID> randomDestination(Beliefs beliefs, Desires desires){


        EntityListBelief buildings = (EntityListBelief)beliefs.getBelief(BeliefType.BUILDINGS);
        ArrayList<StandardEntity> buildingIDs = buildings.getEntities();

        Desire targetBuilding = desires.getDesire(DesireType.GOAL_LOCATION);
        EntityID target = null;

        if(targetBuilding == null){
            target = buildingIDs.get((int)(Math.random()*buildingIDs.size())).getID();
        }else{
            target = targetBuilding.getEntityID();
        }

        List<EntityID> targets = new ArrayList<>();

        targets.add(target);

        desires.addDesire(DesireType.GOAL_LOCATION,new Desire(target));

        List<EntityID> path = searchPlan.createPlan(beliefs,desires);

        return path;
    }
}
