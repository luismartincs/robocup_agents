package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.Desires;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;

public class ReportFirePlan extends AbstractPlan{

    public ReportFirePlan(CinvesAgent agent){
        super(agent);
    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires, Intentions intentions) {

        EntityListBelief buildings = (EntityListBelief)getAgent().getBeliefs().getBelief(BeliefType.BUILDINGS);

        for(StandardEntity entity:buildings.getEntities()){

            Building building = (Building)entity;

            if(building.isOnFire()){

                EntityMapBelief buildingsOnFire = (EntityMapBelief)beliefs.getBelief(BeliefType.BUILDINGS_ON_FIRE);

                if(!buildingsOnFire.contains(building)){
                    buildingsOnFire.addEntity(building);
                }
            }
        }

        return null;
    }
}
