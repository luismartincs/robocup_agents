package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desires;

public abstract  class AbstractPlan {

    protected int time;

    private CinvesAgent agent;

    public AbstractPlan(CinvesAgent agent){
        this.agent = agent;
    }

    public CinvesAgent getAgent(){return agent;}

    public abstract Object createPlan(Beliefs beliefs,Desires desires);


    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
