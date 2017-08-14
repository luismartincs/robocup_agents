package commlib.cinvesframework.belief;

import rescuecore2.worldmodel.ChangeSet;

public class EnvironmentBelief extends Belief{

    private ChangeSet changeSet;

    public EnvironmentBelief(ChangeSet changeSet){
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}
