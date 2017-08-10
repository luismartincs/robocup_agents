package commlib.cinvesframework.belief;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;

public class EntityListBelief extends Belief{

    private ArrayList<StandardEntity> entities;

    public EntityListBelief(){
        entities = new ArrayList<>();
    }

    public void addEntity(StandardEntity entityID){
        entities.add(entityID);
    }

    public ArrayList<StandardEntity> getEntities() {
        return entities;
    }

}
