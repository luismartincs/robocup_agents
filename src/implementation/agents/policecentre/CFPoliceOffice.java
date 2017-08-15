package implementation.agents.policecentre;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import implementation.agents.ActionConstants;
import implementation.agents.policeforce.CFPoliceForce;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

import java.util.Collection;
import java.util.EnumSet;

public class CFPoliceOffice extends CinvesAgent<PoliceOffice> {

    BlockadeList blockadeList;

    @Override
    public void postConnect(){
        super.postConnect();

        blockadeList=new BlockadeList();

        System.out.println("hola soy un police office "+this.getID());

    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time, changed, heard);

        for(ACLMessage msg: this.getAclMessages()){
            if(msg.getPerformative().equals(ACLPerformative.INFORM)){

                BlockadeInfo binfo=new BlockadeInfo(msg.getXPosition(),msg.getYPosition(),msg.getRepairCost(),msg.getBlockade().getValue());
                blockadeList.addBlockade(binfo);


            }else if(msg.getPerformative().equals(ACLPerformative.REQUEST)){

               /* ACLMessage informBlockade = new ACLMessage(time,me().getID(), ACLPerformative.INFORM,new EntityID(msg.gets),
                        msg.getConversationId(), ActionConstants.REQUEST_BLOCKADE,0,0,0,0,new EntityID(32987),0);*/


                StandardEntity entity=getWorldModel().getEntity(new EntityID(81001552));
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

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
    }
}
