package commlib.cinvesframework.belief;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

public class PathBelief extends Belief{

    private List<EntityID> path;

    public PathBelief(){
        path = new ArrayList<>();
    }

    public PathBelief(List<EntityID> path){
        this.path = path;
    }

    public void addEntity(EntityID entityID){
        path.add(entityID);
    }

    public List<EntityID> getPath() {
        return path;
    }

    public void setPath(ArrayList<EntityID> path){
        this.path = path;
    }
}
