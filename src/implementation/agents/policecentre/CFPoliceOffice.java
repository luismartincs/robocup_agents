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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;

public class CFPoliceOffice extends CinvesAgent<PoliceOffice> {

    BlockadeList blockadeList;

    private HashMap<Integer,ArrayList<PoliceForce>> policesByQuadrant;

    public CFPoliceOffice(){
        super(4,new int[]{2,3,4});
    }

    @Override
    public void postConnect(){
        super.postConnect();

        blockadeList=new BlockadeList();

        policesByQuadrant = new HashMap<>();

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
                pfbq.add(policeForce);
                policesByQuadrant.put(quadrant,pfbq);
            }else{
                ArrayList<PoliceForce> pfbq = policesByQuadrant.get(quadrant);
                pfbq.add(policeForce);
                policesByQuadrant.put(quadrant,pfbq);
            }

            System.out.println(se.getID()+" --> "+policeForce.getPosition()+" -- "+quadrant);
        }
    }


    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time, changed, heard);

        for (ACLMessage msg:this.getAclMessages()){
            switch (msg.getPerformative()){
                case INFORM:

                    if(msg.getContent() == ActionConstants.REPORT_BLOCKADE){
                        System.out.println("El usario "+msg.getSender()+" me reporta un bloqueo en "+msg.getExtra(0)+" en cuadrante "+msg.getExtra(1));
                    }

                    break;
            }
        }

/*
        for (Integer q:policesByQuadrant.keySet()) {
            for (PoliceForce pf:policesByQuadrant.get(q)){
                System.out.println("Police ["+q+"] "+pf.getID()+" "+pf.getPosition()+" "+pf.getX());
            }
        }
*/


        /*
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
        }*/

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
    }
}
