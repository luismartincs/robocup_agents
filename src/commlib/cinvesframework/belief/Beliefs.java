package commlib.cinvesframework.belief;

import commlib.cinvesframework.agent.CinvesAgent;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Beliefs {

    private CinvesAgent agent;

    private HashMap<BeliefType,Belief> beliefs;


    private EntityListBelief buildings;
    private EntityMapBelief buildingsOnFire;
    private EntityListBelief areas;

    public Beliefs(CinvesAgent agent){

        this.agent = agent;
        beliefs = new HashMap<>();
        buildings = new EntityListBelief();
        buildingsOnFire = new EntityMapBelief();
        areas = new EntityListBelief();

    }

    public void loadDefaultBeliefs(){

        for(StandardEntity next : agent.getWorldModel()){
            if(next instanceof Building){
                buildings.addEntity(next);
            }
            if(next instanceof Road){
               // roadIDs.add(next.getID());
            }
            if(next instanceof Refuge){
                //refugeIDs.add(next.getID());
            }
            if(next instanceof Area){
                areas.addEntity(next);
            }
        }

        beliefs.put(BeliefType.BUILDINGS,buildings);
        beliefs.put(BeliefType.AREAS,areas);
        beliefs.put(BeliefType.BUILDINGS_ON_FIRE,buildingsOnFire);

    }

    public Belief getBelief(BeliefType beliefType){
        return beliefs.get(beliefType);
    }

    public void addBelief(BeliefType beliefType,Belief belief){
       beliefs.put(beliefType,belief);
    }

}
