package commlib.cinvesframework.desire;


import commlib.cinvesframework.agent.CinvesAgent;

import java.util.HashMap;

public class Desires {

    private CinvesAgent agent;

    private HashMap<DesireType,Desire> desires;

    public Desires(CinvesAgent agent){
        this.agent = agent;
        this.desires = new HashMap<>();
    }

    public Desire getDesire(DesireType desireType){
        return this.desires.get(desireType);
    }

    public void addDesire(DesireType desireType,Desire desire){
        this.desires.put(desireType,desire);
    }

    public void removeDesire(DesireType desireType){
        this.desires.remove(desireType);
    }

    public HashMap<DesireType, Desire> getDesires() {
        return desires;
    }

    public void setDesires(HashMap<DesireType, Desire> desires) {
        this.desires = desires;
    }
}
