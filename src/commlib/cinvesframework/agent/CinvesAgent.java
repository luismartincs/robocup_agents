package commlib.cinvesframework.agent;

import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.Beliefs;
import commlib.components.AbstractCSAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;


public abstract class CinvesAgent <E extends StandardEntity>  extends AbstractCSAgent<E>{

    private Beliefs beliefs;


    protected CinvesAgent(){

        beliefs = new Beliefs(this);

    }

    /**
     * Make getModel public
     */

    public StandardWorldModel getWorldModel(){
        return this.model;
    }


    @Override
    protected void postConnect(){
        super.postConnect();
    }
}
