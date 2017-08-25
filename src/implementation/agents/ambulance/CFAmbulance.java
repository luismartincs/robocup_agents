package implementation.agents.ambulance;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityMapBelief;
import commlib.cinvesframework.belief.EnvironmentBelief;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.policeforce.LeaderElectionPlan;
import implementation.agents.policeforce.PolicePlan;
import implementation.agents.policeforce.RequestReplyPlan;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;
@SuppressWarnings("Duplicates")
public class CFAmbulance extends CinvesAgent<AmbulanceTeam>{

    private LeaderElectionPlan leaderElectionPlan;
    private AmbulancePlan ambulancePlan;
    private AmbulancePlanNoLeader ambulancePlanNoLeader;

    public CFAmbulance(){
        super(2,new int[]{1,2});
    }

    @Override
    protected void postConnect() {
        super.postConnect();

        System.out.println("conectado ambulance"+this.getID()+"     "+"[quadrant : "+quadrant+"]");

        leaderElectionPlan = new LeaderElectionPlan(this);
        ambulancePlan = new AmbulancePlan(this);
        ambulancePlanNoLeader = new AmbulancePlanNoLeader(this);

        Belief isVolunteer = new Belief();
        isVolunteer.setDataBoolean(false);
        getBeliefs().addBelief(BeliefType.VOLUNTEER,isVolunteer);

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        generalBehaviour(time,changed,heard);
    }

    @Override
    protected void onRegularHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        //super.onRegularHealthBehaviour(time, changed, heard);
        generalBehaviour(time,changed,heard);
    }

    @Override
    protected void onLowHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        //super.onLowHealthBehaviour(time, changed, heard);
        generalBehaviour(time,changed,heard);
    }

    private void generalBehaviour(int time,ChangeSet changed, Collection<Command> heard){


        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));


        leaderElectionPlan.setTime(time);

        Object leaderElected = leaderElectionPlan.createPlan(getBeliefs(),getDesires(),intentions);


        if(leaderElected != null){

            if(leaderElectionPlan.imLeader()) {
                GeneralUtils.updateBuildingsInQuadrant(getBeliefs(),getWorldModel(),quadrant);
            }

            ambulancePlan.setNextQuadrantLeaders(leaderElectionPlan.getNextQuadrantLeaders()); //Pasar esto a beliefs, ahorita no pk urge
            ambulancePlan.setTime(time);
            ambulancePlan.createPlan(getBeliefs(),getDesires(),intentions);
        }else{
            GeneralUtils.updateBuildingsInQuadrant(getBeliefs(),getWorldModel(),quadrant);
            ambulancePlanNoLeader.setTime(time);
            ambulancePlanNoLeader.createPlan(getBeliefs(),getDesires(),intentions);
        }


    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
    }
}
