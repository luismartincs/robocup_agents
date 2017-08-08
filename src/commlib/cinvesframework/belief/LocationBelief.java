package commlib.cinvesframework.belief;

import rescuecore2.worldmodel.EntityID;

public class LocationBelief extends Belief{

    private EntityID entityID;

    public LocationBelief(EntityID entityID){
        this.setEntityID(entityID);
    }

    public EntityID getEntityID() {
        return entityID;
    }

    public void setEntityID(EntityID entityID) {
        this.entityID = entityID;
    }
}
