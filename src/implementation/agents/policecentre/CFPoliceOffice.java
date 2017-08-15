package implementation.agents.policecentre;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import implementation.agents.ActionConstants;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

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

               // System.out.println("agregada información de blockade "+msg.getBlockade().getValue());

            }else if(msg.getPerformative().equals(ACLPerformative.REQUEST)){

                ACLMessage informBlockade = new ACLMessage(time,me().getID(), ACLPerformative.INFORM,new EntityID(msg.getSender()),
                        msg.getConversationId(), ActionConstants.REQUEST_BLOCKADE,0,0,0,0,new EntityID(32987),0);

                addACLMessage(informBlockade);

                System.out.println("El agente "+msg.getSender()+" me solicida un bloqueo");
            }
        }

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
    }
}
