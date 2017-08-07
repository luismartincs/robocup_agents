package commlib.cinvesframework.belief;

import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;

public class EntityListBelief extends Belief{

    private ArrayList<EntityID> entities;

    public EntityListBelief(){
        entities = new ArrayList<>();
    }

    public void addEntity(EntityID entityID){
        entities.add(entityID);
    }

    public ArrayList<EntityID> getEntities() {
        return entities;
    }

}
