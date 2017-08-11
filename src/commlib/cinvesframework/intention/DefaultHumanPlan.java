package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desires;

public class DefaultHumanPlan extends AbstractPlan{

    public DefaultHumanPlan(CinvesAgent agent){
        super(agent);
    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {
        return null;
    }
}
