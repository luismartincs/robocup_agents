package implementation.agents.firebrigade;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.utils.GeneralUtils;
import implementation.agents.Quadrant;
import implementation.agents.config.BeliefsName;
import implementation.agents.firebrigade.FireBrigadePlan;
import implementation.agents.policeforce.LeaderElectionPlan;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

public class CFFireBrigade extends CinvesAgent<FireBrigade> {

    private static final String	MAX_WATER_KEY			= "fire.tank.maximum";
    private static final String	MAX_DISTANCE_KEY	= "fire.extinguish.max-distance";
    private static final String	MAX_POWER_KEY			= "fire.extinguish.max-sum";

    private int									maxWater;
    private int									maxDistance;
    private int									maxPower;

    //private PolicePlan policePlan;//esto se va y se pone un fireBrigadePlan
    private LeaderElectionPlan leaderElectionPlan;

    private FireBrigadePlan fireBrigadePlan;
    private FireBrigadePlanNoLeader fireBrigadePlanNoLeader;

    /**
     * Local keys
     */

    public static final String HAS_WATER = "HAS_WATER";
    public static final String RELOAD = "RELOAD";
    public static final String OLD_GOAL = "OLD_GOAL";
    public static final String RETURN = "RETURN";
    public static final String FIRE_INY = "FIRE_INY";
    public static final String REPORTED_FIRE = "REPORTED_FIRE";

    public CFFireBrigade(){
        super(5,new int[]{1,5});
    }

    @Override
    protected void postConnect() {
        super.postConnect();

        System.out.println("conectado Fire Brigade "+this.getID()+"     "+"[quadrant : "+quadrant+"]");

        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);

        //System.out.println("los max Water, power y distances de los bomberos en CFFIREBRIGADE son: " + maxWater + ", " + maxPower + ", " +  maxDistance );


        leaderElectionPlan = new LeaderElectionPlan(this);
        fireBrigadePlan = new FireBrigadePlan(this);
        fireBrigadePlanNoLeader = new FireBrigadePlanNoLeader(this);


        Belief removeBlockades = new Belief();
        removeBlockades.setDataBoolean(true);

        getBeliefs().addBelief(BeliefType.VOLUNTEER,removeBlockades);
        getBeliefs().addBelief(BeliefType.REPORTED_FIRES,new EntityMapBelief());

        Collection<StandardEntity> hyd = getWorldModel().getEntitiesOfType(StandardEntityURN.HYDRANT);
        ArrayList<StandardEntity> hydrants = new ArrayList<>(hyd);

        getBeliefs().addBelief(BeliefsName.HYDRANTS,hydrants);
        getBeliefs().addBelief(BeliefsName.REQUIRED_WATER,10);
        getBeliefs().addBelief(HAS_WATER,true);
        beliefs.addBelief(FIRE_INY,0);
        beliefs.addBelief(REPORTED_FIRE,new ArrayList<Integer>());

        intentions.addIntention(RELOAD,false);
        intentions.addIntention(CFFireBrigade.RETURN,false);

    }

    @Override
    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {

        defaultBehaviour(time, changed, heard);

    }


    @Override
    protected void onRegularHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        defaultBehaviour(time, changed, heard);
    }

    @Override
    protected void onLowHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard) {
        defaultBehaviour(time, changed, heard);
    }

    private void defaultBehaviour(int time, ChangeSet changed, Collection<Command> heard){

        getBeliefs().addBelief(BeliefType.CHANGED_ENVIRONMENT,new EnvironmentBelief(changed));

        leaderElectionPlan.setTime(time);

        Object leaderElected = leaderElectionPlan.createPlan(getBeliefs(),getDesires(),intentions);

        //System.out.println("Bombero en onFullHealthBehavior");

        if(leaderElected != null){

            /**
             * Si eres el lider del cuadrante actualizas tus creencias sobre los edificios que te corresponden
             */

            if(leaderElectionPlan.imLeader()) {
                GeneralUtils.updateRoadsInQuadrant(getBeliefs(),getWorldModel(),quadrant);
            }

            fireBrigadePlan.setNextQuadrantLeaders(leaderElectionPlan.getNextQuadrantLeaders()); //Pasar esto a beliefs, ahorita no pk urge
            fireBrigadePlan.setTime(time);
            fireBrigadePlan.createPlan(getBeliefs(),getDesires(),intentions);
        }else {
            fireBrigadePlanNoLeader.setTime(time);
            fireBrigadePlanNoLeader.createPlan(beliefs,desires,intentions);
        }

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
