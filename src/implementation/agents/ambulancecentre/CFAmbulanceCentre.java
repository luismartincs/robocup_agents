package implementation.agents.ambulancecentre;

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

public class CFAmbulanceCentre extends CinvesAgent<AmbulanceCentre> {

    private HashMap<Integer,ArrayList<AmbulanceTeam>> ambulanceByQuadrant;
    private HashMap<Integer,ArrayList<Integer>> ambulanceIDByQuadrant;
    private HashMap<Integer,Integer> leaderByQuadrant;
    private boolean leaderElected = false;
    private int lidersEnviados = 0;

    public CFAmbulanceCentre(){
        super(2,new int[]{2});
    }

    @Override
    public void postConnect(){
        super.postConnect();

        ambulanceByQuadrant = new HashMap<>();
        ambulanceIDByQuadrant = new HashMap<>();
        leaderByQuadrant = new HashMap<>();
        loadAmbulances();

    }

    private void loadAmbulances(){

        Collection<StandardEntity> ambulances = getWorldModel().getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);

        for (StandardEntity se:ambulances){

            AmbulanceTeam ambulanceTeam = (AmbulanceTeam)se;
            ambulanceTeam.getPosition();

            Pair<Integer,Integer> point = ambulanceTeam.getLocation(getWorldModel());

            int px=point.first();
            int py=point.second();

            quadrant= Quadrant.getQuadrant(getWorldModel(),px,py);

            if(ambulanceByQuadrant.get(quadrant) == null){

                ArrayList<AmbulanceTeam> pfbq = new ArrayList<>();
                ArrayList<Integer> atbid = new ArrayList<>();

                pfbq.add(ambulanceTeam);
                atbid.add(ambulanceTeam.getID().getValue());

                ambulanceByQuadrant.put(quadrant,pfbq);
                ambulanceIDByQuadrant.put(quadrant,atbid);

            }else{

                ArrayList<AmbulanceTeam> pfbq = ambulanceByQuadrant.get(quadrant);
                ArrayList<Integer> atbid = ambulanceIDByQuadrant.get(quadrant);

                pfbq.add(ambulanceTeam);
                atbid.add(ambulanceTeam.getID().getValue());

                ambulanceByQuadrant.put(quadrant,pfbq);
                ambulanceIDByQuadrant.put(quadrant,atbid);
            }

            System.out.println(se.getID()+" --> "+ambulanceTeam.getPosition()+" -- "+quadrant);
        }
    }

    private void sortEntities(){

        for(Integer quadrant:ambulanceByQuadrant.keySet()){
            ArrayList<Integer> elements = ambulanceIDByQuadrant.get(quadrant);
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
        return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
    }
}
