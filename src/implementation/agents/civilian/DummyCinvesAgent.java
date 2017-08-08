package implementation.agents.civilian;

import commlib.bdi.messages.ACLMessage;
import commlib.bdi.messages.ACLPerformative;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.LocationBelief;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.message.RCRSCSMessage;
import commlib.task.pf.ClearRouteTaskMessage;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
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

        ACLMessage message = new ACLMessage(time,getID(),ACLPerformative.CFP); //,new EntityID(531016945));
        System.out.println("ME "+getID());
        List<EntityID> steps = plan.createPlan(getBeliefs(),getDesires());

        addMessage(message);

        sendMove(time,steps);

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

}
