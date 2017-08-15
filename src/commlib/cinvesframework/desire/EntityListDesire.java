package commlib.cinvesframework.desire;

import commlib.cinvesframework.belief.Belief;
import rescuecore2.standard.entities.StandardEntity;

import java.util.ArrayList;

public class EntityListDesire extends Desire{

    private ArrayList<StandardEntity> entities;

    public EntityListDesire(){
        entities = new ArrayList<>();
    }

    public void addEntity(StandardEntity entityID){
        entities.add(entityID);
    }

    public void removeEntity(StandardEntity entityID){
        entities.remove(entityID);
    }

    public ArrayList<StandardEntity> getEntities() {
        return entities;
    }

}
