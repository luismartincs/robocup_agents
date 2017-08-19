package implementation.agents.civilian;

import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityMapBelief;
import commlib.cinvesframework.belief.EnvironmentBelief;
import commlib.cinvesframework.intention.GoToRefugePlan;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.utils.GeneralUtils;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;

public class CFCivilian extends CinvesAgent<Civilian>{

    private final double BECOMES_VOLUNTEER = 0.30;

    private CivilianPlan plan;
    private GoToRefugePlan refugePlan;

    @Override
    protected void postConnect() {
        super.postConnect();

        plan = new CivilianPlan(this);
        refugePlan = new GoToRefugePlan(this);

        Belief isVolunteer = new Belief();

        if(Math.random() < BECOMES_VOLUNTEER){
            isVolunteer.setDataBoolean(true);
        }else{
            isVolunteer.setDataBoolean(false);
        }

        getBeliefs().addBelief(BeliefType.VOLUNTEER,isVolunteer);
        getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());
    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        plan.setTime(time);
        plan.createPlan(getBeliefs(),getDesires());

    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

}
