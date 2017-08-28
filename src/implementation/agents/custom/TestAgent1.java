package implementation.agents.custom;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityMapBelief;
import commlib.cinvesframework.belief.EnvironmentBelief;
import commlib.cinvesframework.intention.GoToRefugePlan;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.messages.TestMessage;
import implementation.agents.ActionConstants;
import implementation.agents.civilian.CivilianPlan;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.EnumSet;

public class TestAgent1 extends CinvesAgent<PoliceForce> {

    private final double BECOMES_VOLUNTEER = 0.30;

    private CivilianPlan plan;
    private GoToRefugePlan refugePlan;

    public TestAgent1(){
        super(1,new int[]{1,2,3,5});
    }

    @Override
    protected void postConnect() {
        super.postConnect();

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        for(TestMessage msg:this.receivedMessageListACL){
            System.out.println(msg.getSender()+" "+msg.getContent());
        }


        ACLMessage leaderCFP = new ACLMessage(time,
                getID(),
                ACLPerformative.INFORM,
                new EntityID(0),
                0,
                ActionConstants.REQUEST_POLICE_INSTRUCTION,
                0);

        TestMessage msg = new TestMessage(getID().getValue(),"Hola soy "+getID().getValue());

        addTestMessage(msg);

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
