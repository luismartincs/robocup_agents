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
    private EntityListBelief refuges;
    private EntityListBelief policeCentre;
    private EntityListBelief roads;

    public Beliefs(CinvesAgent agent){

        this.agent = agent;
        beliefs = new HashMap<>();
        buildings = new EntityListBelief();
        refuges = new EntityListBelief();
        buildingsOnFire = new EntityMapBelief();
        areas = new EntityListBelief();
        policeCentre = new EntityListBelief();
        roads = new EntityListBelief();

    }

    public void loadDefaultBeliefs(){

        for(StandardEntity next : agent.getWorldModel()){
            if(next instanceof Building){
                buildings.addEntity(next);
            }
            if(next instanceof Road){
                roads.addEntity(next);
            }
            if(next instanceof Refuge){
                refuges.addEntity(next);
            }
            if(next instanceof Area){
                areas.addEntity(next);
            }
            if(next instanceof PoliceOffice){
                policeCentre.addEntity(next);
            }

        }

        beliefs.put(BeliefType.BUILDINGS,buildings);
        beliefs.put(BeliefType.AREAS,areas);
        beliefs.put(BeliefType.REFUGE,refuges);
        beliefs.put(BeliefType.BUILDINGS_ON_FIRE,buildingsOnFire);
        beliefs.put(BeliefType.POLICE_CENTRE,policeCentre);

        SaveBeliefs.saveBeliefs(buildings,roads,agent.getWorldModel());

    }

    public Belief getBelief(BeliefType beliefType){
        return beliefs.get(beliefType);
    }

    public void addBelief(BeliefType beliefType,Belief belief){
       beliefs.put(beliefType,belief);
    }

}
