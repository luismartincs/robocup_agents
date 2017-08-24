package implementation.agents.firebrigade;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.Quadrant;
import implementation.agents.firebrigade.FireBrigadePlan;
import implementation.agents.policeforce.LeaderElectionPlan;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.EnumSet;

public class CFFireBrigade extends CinvesAgent<PoliceForce> {

    private static final String	MAX_WATER_KEY			= "fire.tank.maximum";
    private static final String	MAX_DISTANCE_KEY	= "fire.extinguish.max-distance";
    private static final String	MAX_POWER_KEY			= "fire.extinguish.max-sum";

    private int									maxWater;
    private int									maxDistance;
    private int									maxPower;

    //private PolicePlan policePlan;//esto se va y se pone un fireBrigadePlan
    private LeaderElectionPlan leaderElectionPlan;

    private FireBrigadePlan fireBrigadePlan;

    @Override
    protected void postConnect() {
        super.postConnect();

        System.out.println("conectado Fire Brigade "+this.getID()+"     "+"[quadrant : "+quadrant+"]");

        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);

        //System.out.println("los max Water, power y distances de los bomberos en CFFIREBRIGADE son: " + maxWater + ", " + maxPower + ", " +  maxDistance );


        //policePlan = new PolicePlan(this); //NOTE: este cambia por el FireBrigadePlan.
        leaderElectionPlan = new LeaderElectionPlan(this);
        fireBrigadePlan = new FireBrigadePlan(this);


        Belief removeBlockades = new Belief();
        removeBlockades.setDataBoolean(true);

        getBeliefs().addBelief(BeliefType.VOLUNTEER,removeBlockades);
        //getBeliefs().addBelief(BeliefType.REPORTED_BLOCKADES,new EntityMapBelief());//NOTE: Según yo esto ya no va.

        getBeliefs().addBelief(BeliefType.REPORTED_FIRES,new EntityMapBelief());

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        leaderElectionPlan.setTime(time);

        Object leaderElected = leaderElectionPlan.createPlan(getBeliefs(),getDesires());

        //System.out.println("Bombero en onFullHealthBehavior");

        if(leaderElected != null){

            /**
             * Si eres el lider del cuadrante actualizas tus creencias sobre los edificios que te corresponden
             */

            if(leaderElectionPlan.imLeader()) {
                GeneralUtils.updateBuildingsInQuadrant(getBeliefs(),getWorldModel(),quadrant);
            }

            fireBrigadePlan.setNextQuadrantLeaders(leaderElectionPlan.getNextQuadrantLeaders()); //Pasar esto a beliefs, ahorita no pk urge
            fireBrigadePlan.setTime(time);
            fireBrigadePlan.createPlan(getBeliefs(),getDesires());
        }
    }


    @Override
    protected void onRegularHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        super.onRegularHealthBehaviour(time, changed, heard);
        System.out.println("Regular...");
    }

    @Override
    protected void onLowHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        super.onLowHealthBehaviour(time, changed, heard);
        System.out.println("Low...");
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    public int getMaxWater()
    {return  maxWater;}

    public int getMaxPower()
    {return  maxPower;}

    public int getMaxDistance()
    {return  maxDistance;}

}
