package commlib.cinvesframework.belief;

import rescuecore2.standard.entities.StandardEntity;

import java.util.ArrayList;
import java.util.HashMap;

public class EntityMapBelief extends Belief{

    private HashMap<Integer,StandardEntity> entities;

    public EntityMapBelief(){
        entities = new HashMap<>();
    }

    public void addEntity(StandardEntity entity){
        entities.put(entity.getID().getValue(),entity);
    }

    public HashMap<Integer,StandardEntity> getEntities() {
        return entities;
    }

    public boolean contains(StandardEntity entity){
        return entities.containsKey(entity.getID().getValue());
    }

    //test.

}