package implementation.agents.firestation;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import implementation.agents.ActionConstants;
import implementation.agents.Quadrant;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.*;

@SuppressWarnings("Duplicates")

public class CFFireStation extends CinvesAgent<FireStation> {

    private HashMap<Integer,ArrayList<FireBrigade>> firebrigadeByQuadrant;
    private HashMap<Integer,ArrayList<Integer>> firebrigadeIDByQuadrant;
    private HashMap<Integer,Integer> leaderByQuadrant;
    private boolean leaderElected = false;
    private int lidersEnviados = 0;

    public CFFireStation(){
        super(5,new int[]{5});
    }

    @Override
    public void postConnect(){
        super.postConnect();

        firebrigadeByQuadrant = new HashMap<>();
        firebrigadeIDByQuadrant = new HashMap<>();
        leaderByQuadrant = new HashMap<>();
        loadFireBrigades();

    }

    private void loadFireBrigades(){

        Collection<StandardEntity> firebrigades = getWorldModel().getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);

        for (StandardEntity se:firebrigades){

            FireBrigade fireBrigade = (FireBrigade)se;
            fireBrigade.getPosition();

            Pair<Integer,Integer> point = fireBrigade.getLocation(getWorldModel());

            int px=point.first();
            int py=point.second();

            quadrant= Quadrant.getQuadrant(getWorldModel(),px,py);

            if(firebrigadeByQuadrant.get(quadrant) == null){

                ArrayList<FireBrigade> pfbq = new ArrayList<>();
                ArrayList<Integer> atbid = new ArrayList<>();

                pfbq.add(fireBrigade);
                atbid.add(fireBrigade.getID().getValue());

                firebrigadeByQuadrant.put(quadrant,pfbq);
                firebrigadeIDByQuadrant.put(quadrant,atbid);

            }else{

                ArrayList<FireBrigade> pfbq = firebrigadeByQuadrant.get(quadrant);
                ArrayList<Integer> atbid = firebrigadeIDByQuadrant.get(quadrant);

                pfbq.add(fireBrigade);
                atbid.add(fireBrigade.getID().getValue());

                firebrigadeByQuadrant.put(quadrant,pfbq);
                firebrigadeIDByQuadrant.put(quadrant,atbid);
            }

            System.out.println(se.getID()+" --> "+fireBrigade.getPosition()+" -- "+quadrant);
        }
    }

    private void sortEntities(){

        for(Integer quadrant: firebrigadeByQuadrant.keySet()){
            ArrayList<Integer> elements = firebrigadeIDByQuadrant.get(quadrant);
            Collections.sort(elements);

            int lider = elements.get(elements.size()-1);

            leaderByQuadrant.put(quadrant,lider);

            System.out.println("El lider es: "+lider);
        }

        leaderElected = true;

    }

    private void informLeader(int time, int leader,int quadrant){

        ACLMessage propose = new ACLMessage(time,getID(),
                ACLPerformative.INFORM,
                new EntityID(0),
                nextConversationId(),
                ActionConstants.LEADER_ELECTION,
                leader,
                quadrant);

        addACLMessage(propose);
    }

    private void informLeaders(int time){

        for(Integer quadrant:leaderByQuadrant.keySet()){

            int leader = leaderByQuadrant.get(quadrant);

            informLeader(time,leader,quadrant);
        }

    }

    @Override
    protected void doCentreAction(int time, ChangeSet changed, Collection<Command> heard) {

        if(!leaderElected) {
            sortEntities();
            informLeaders(time);
        }

        /*
        if(lidersEnviados < leaderByQuadrant.size()) {

            System.out.println("Enviando lider [" + leaderByQuadrant.get(lidersEnviados) + "] a cuadrante " + lidersEnviados + " en tiempo " + time);

            lidersEnviados++;
        }*/
    }


    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_STATION);
    }
}
