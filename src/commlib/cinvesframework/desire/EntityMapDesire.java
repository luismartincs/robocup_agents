package commlib.cinvesframework.desire;

import rescuecore2.standard.entities.StandardEntity;

import java.util.HashMap;

public class EntityMapDesire extends Desire{

    private HashMap<Integer,StandardEntity> entities;

    public EntityMapDesire(){
        entities = new HashMap<>();
    }


    public void addEntity(StandardEntity entity){
        entities.put(entity.getID().getValue(),entity);
    }

    public void removeEntity(StandardEntity entity){
        entities.remove(entity.getID().getValue());
    }

    public HashMap<Integer,StandardEntity> getEntities() {
        return entities;
    }

    public boolean contains(StandardEntity entity){
        return entities.containsKey(entity.getID().getValue());
    }
}
