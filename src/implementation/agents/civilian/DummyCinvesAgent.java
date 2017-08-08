package implementation.agents.civilian;

import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class DummyCinvesAgent extends CinvesAgent<Civilian>{

    private CivilianPlan plan;

    @Override
    protected void postConnect() {
        super.postConnect();

        plan = new CivilianPlan(this);

    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        super.thinking(time,changed,heard);

        ACLMessage message = new ACLMessage(time,getID(),ACLPerformative.CFP,new EntityID(531016945),20,10);

        getDesires().addDesire(DesireType.GOAL_LOCATION,new Desire(new EntityID(4335)));

        List<EntityID> steps = plan.createPlan(getBeliefs(),getDesires());

        addMessage(message);

        sendMove(time,steps);

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

}
