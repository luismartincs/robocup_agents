package implementation.agents.policecentre;

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

import javax.swing.*;
import java.util.*;

@SuppressWarnings("Duplicates")
public class CFPoliceOffice extends CinvesAgent<PoliceOffice> {

    BlockadeList blockadeList;

    private HashMap<Integer,ArrayList<PoliceForce>> policesByQuadrant;
    private HashMap<Integer,ArrayList<Integer>>policeIDByQuadrant;
    private HashMap<Integer,Integer> leaderByQuadrant;
    private HashMap<Integer,Integer> availableByQuadrant;
    private boolean leaderElected = false;

    public CFPoliceOffice(){
        super(4,new int[]{2,3,4});
    }

    @Override
    public void postConnect(){
        super.postConnect();

        blockadeList=new BlockadeList();

        policesByQuadrant = new HashMap<>();
        policeIDByQuadrant = new HashMap<>();
        leaderByQuadrant = new HashMap<>();
        availableByQuadrant = new HashMap<>();

        loadPolices();

    }

    private void loadPolices(){

        Collection<StandardEntity> policeForces = getWorldModel().getEntitiesOfType(StandardEntityURN.POLICE_FORCE);

        for (StandardEntity se:policeForces){

            PoliceForce policeForce = (PoliceForce)se;
            policeForce.getPosition();

            Pair<Integer,Integer> point = policeForce.getLocation(getWorldModel());

            int px=point.first();
            int py=point.second();

            quadrant= Quadrant.getQuadrant(getWorldModel(),px,py);

            if(policesByQuadrant.get(quadrant) == null){
                ArrayList<PoliceForce> pfbq = new ArrayList<>();
                ArrayList<Integer> atbid = new ArrayList<>();

                pfbq.add(policeForce);
                atbid.add(policeForce.getID().getValue());

                policesByQuadrant.put(quadrant,pfbq);
                policeIDByQuadrant.put(quadrant,atbid);

                availableByQuadrant.put(quadrant,0);
            }else{
                ArrayList<PoliceForce> pfbq = policesByQuadrant.get(quadrant);
                ArrayList<Integer> atbid = policeIDByQuadrant.get(quadrant);

                pfbq.add(policeForce);
                atbid.add(policeForce.getID().getValue());

                policesByQuadrant.put(quadrant,pfbq);
                policeIDByQuadrant.put(quadrant,atbid);
            }

            System.out.println(se.getID()+" --> "+policeForce.getPosition()+" -- "+quadrant);
        }
    }

    private void sortEntities(){

        for(Integer quadrant:policeIDByQuadrant.keySet()){
            ArrayList<Integer> elements = policeIDByQuadrant.get(quadrant);
            Collections.sort(elements);

            int lider = elements.get(elements.size()-1);

            leaderByQuadrant.put(quadrant,lider);

            System.out.println("El lider es: "+lider);
        }

        leaderElected = true;

    }

    private void asignar(int quadrant,int location, int time){

        ArrayList<PoliceForce> polices = policesByQuadrant.get(quadrant);
        int available = availableByQuadrant.get(quadrant);

        if(available >= polices.size()){
            available = 0;
        }

        PoliceForce pf = polices.get(available);

        available++;

        int conversationId = nextConversationId();

        ACLMessage leaderCFP = new ACLMessage(time,
                getID(),
                ACLPerformative.INFORM,
                pf.getID(),
                conversationId,
                ActionConstants.INFORM_BLOCKADE,
                location);

        addACLMessage(leaderCFP);


        availableByQuadrant.put(quadrant,available);

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
        for (ACLMessage msg:this.getAclMessages()){
            switch (msg.getPerformative()){
                case INFORM:

                    if(msg.getContent() == ActionConstants.REPORT_BLOCKADE){
                        // System.out.println("El usario "+msg.getSender()+" me reporta un bloqueo en "+msg.getExtra(0)+" en cuadrante "+msg.getExtra(1));
                        asignar(msg.getExtra(1),msg.getExtra(0),time);
                    }

                    break;
            }
        }

        if(!leaderElected) {
            sortEntities();
            informLeaders(time);
        }
    }

    /*
    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time, changed, heard);




        for (Integer q:policesByQuadrant.keySet()) {
            for (PoliceForce pf:policesByQuadrant.get(q)){
                System.out.println("Police ["+q+"] "+pf.getID()+" "+pf.getPosition()+" "+pf.getX());
            }
        }




        for(ACLMessage msg: this.getAclMessages()){
            if(msg.getPerformative().equals(ACLPerformative.INFORM)){

                BlockadeInfo binfo=new BlockadeInfo(msg.getXPosition(),msg.getYPosition(),msg.getRepairCost(),msg.getBlockade().getValue());
                blockadeList.addBlockade(binfo);


            }else if(msg.getPerformative().equals(ACLPerformative.REQUEST)){


                StandardEntity entity=getWorldModel().getEntity(new EntityID(msg.getSender()));
                int px=Integer.parseInt(entity.getProperty("urn:rescuecore2.standard:property:x").getValue().toString());
                int py=Integer.parseInt(entity.getProperty("urn:rescuecore2.standard:property:y").getValue().toString());
                BlockadeInfo nearBlockade=blockadeList.getNearestBlockeade( entity.getID(),px,py);
                if(nearBlockade!=null){
                    ACLMessage informBlockade = new ACLMessage(time,me().getID(), ACLPerformative.INFORM,new EntityID(msg.getSender()),
                            msg.getConversationId(), ActionConstants.REQUEST_BLOCKADE,0,0,0,0,nearBlockade.getBlockade().getID(),0);
                    addACLMessage(informBlockade);
                }


                System.out.println("El agente "+msg.getSender()+" me solicida un bloqueo");
            }
        }

    }*/

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
    }
}
