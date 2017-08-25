package implementation.agents.policeforce;

import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.intention.SearchPlan;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.Quadrant;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("Duplicates")
public class CFPoliceForce extends CinvesAgent<PoliceForce> {

    private PolicePlan policePlan;
    private LeaderElectionPlan leaderElectionPlan;

    private RequestReplyPlan requestReplyPlan;

    public CFPoliceForce(){
        super(3,new int[]{1,3,4});
    }

    @Override
    protected void postConnect() {
        super.postConnect();

        policePlan = new PolicePlan(this);
        leaderElectionPlan = new LeaderElectionPlan(this);
        requestReplyPlan = new RequestReplyPlan(this);


        Belief removeBlockades = new Belief();
        removeBlockades.setDataBoolean(true);

        getBeliefs().addBelief(BeliefType.VOLUNTEER,removeBlockades);
        getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        /*
        policePlan.setTime(time);
        policePlan.createPlan(getBeliefs(),getDesires());
        */

        leaderElectionPlan.setTime(time);

        Object leaderElected = leaderElectionPlan.createPlan(getBeliefs(),getDesires(),intentions);

        if(leaderElected != null){

            /**
             * Si eres el lider del cuadrante actualizas tus creencias sobre los edificios que te corresponden
             */

            if(leaderElectionPlan.imLeader()) {

                if (getBeliefs().getBelief(BeliefType.BUILDINGS_IN_QUADRANT) == null) {

                    EntityListBelief buildingsInQuadrant = new EntityListBelief();
                    EntityListBelief buildings = (EntityListBelief) getBeliefs().getBelief(BeliefType.BUILDINGS);

                    for (StandardEntity building : buildings.getEntities()) {

                        Pair<Integer, Integer> point = building.getLocation(getWorldModel());

                        int px = point.first();
                        int py = point.second();
                        int q = Quadrant.getQuadrant(getWorldModel(), px, py);

                        if (q == quadrant) {
                            buildingsInQuadrant.addEntity(building);
                        }

                    }
                    getBeliefs().addBelief(BeliefType.BUILDINGS_IN_QUADRANT, buildingsInQuadrant);
                }

            }

            requestReplyPlan.setTime(time);
            requestReplyPlan.createPlan(getBeliefs(),getDesires(),intentions);


        }
        else{
            /**
             * The police force will move to the nearest road, and if it is blocked, the agent will remove it
             */
            Road road=GeneralUtils.getRoad(this);
            if(road!=null) {
                getDesires().addDesire(DesireType.GOAL_LOCATION, new Desire(road.getID()));
                requestReplyPlan.setTime(time);
                requestReplyPlan.OfflinePlan(getBeliefs(), getDesires(),changed);
            }
        }

    }


    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
}
