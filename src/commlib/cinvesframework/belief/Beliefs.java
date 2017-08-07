package commlib.cinvesframework.belief;

import commlib.cinvesframework.agent.CinvesAgent;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Beliefs {

    private CinvesAgent agent;

    private HashMap<BeliefType,Belief> beliefs;


    private EntityListBelief buildings;

    public Beliefs(CinvesAgent agent){

        this.agent = agent;
        beliefs = new HashMap<>();
        buildings = new EntityListBelief();

        loadDefaultBeliefs();
    }

    private void loadDefaultBeliefs(){

        for(StandardEntity next : agent.getWorldModel()){
            if(next instanceof Building){
                buildings.addEntity(next.getID());
            }
            if(next instanceof Road){
               // roadIDs.add(next.getID());
            }
            if(next instanceof Refuge){
                //refugeIDs.add(next.getID());
            }
        }

        beliefs.put(BeliefType.BUILDINGS,buildings);

    }

    public Belief getBelief(BeliefType beliefType){
        return beliefs.get(beliefType);
    }

    public void addBelief(Belief belief){
       // beliefs.put()
    }

}
