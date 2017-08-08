package commlib.cinvesframework.desire;

import rescuecore2.worldmodel.EntityID;

public class Desire {

    private EntityID entityID;

    public Desire(EntityID entityID){
        this.entityID = entityID;
    }

    public EntityID getEntityID() {
        return entityID;
    }

    public void setEntityID(EntityID entityID) {
        this.entityID = entityID;
    }
}
