package implementation.agents.policecentre;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.message.RCRSCSMessage;
import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;

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

        for(ACLMessage msg:this.aclMessages){
            if(msg.getPerformative().equals(ACLPerformative.INFORM)){

                BlockadeInfo binfo=new BlockadeInfo(msg.getXPosition(),msg.getYPosition(),msg.getRepairCost(),msg.getBlockade().getValue());
                blockadeList.addBlockade(binfo);

                System.out.println("agregada información de blockade ");

            }
        }

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
    }
}
