package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;

import java.util.HashMap;

public class Intentions {

    private HashMap<String,Object> intentions;
    private CinvesAgent agent;

    public Intentions(CinvesAgent agent){
        this.agent = agent;
        intentions = new HashMap<>();
    }

    public void addIntention(String name,Object data){
        intentions.put(name,data);
    }

    public Object getIntention(String name){
        return intentions.get(name);
    }
}
