package commlib.cinvesframework.intention;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.belief.EntityListBelief;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.*;

public class SearchPlan extends AbstractPlan {

    private Map<EntityID, Set<EntityID>> graph;

    public SearchPlan(CinvesAgent agent) {

        super(agent);

        Map<EntityID, Set<EntityID>> neighbours = new LazyMap<EntityID, Set<EntityID>>() {
            @Override
            public Set<EntityID> createValue() {
                return new HashSet<EntityID>();
            }
        };

        EntityListBelief listBelief = (EntityListBelief) agent.getBeliefs().getBelief(BeliefType.AREAS);

        for (StandardEntity next : listBelief.getEntities()) {
            Collection<EntityID> areaNeighbours = ((Area) next).getNeighbours();
            neighbours.get(next.getID()).addAll(areaNeighbours);
        }

        setGraph(neighbours);

    }


    @Override
    public List<EntityID> createPlan(Beliefs beliefs, Desires desires, Intentions intentions) {

        Desire goal = desires.getDesire(DesireType.GOAL_LOCATION);

        EntityID position = ((Human)getAgent().me()).getPosition();

        List<EntityID> steps = breadthFirstSearch(beliefs,desires,position,goal.getEntityID());

        return steps;
    }

    public List<EntityID> createPlan(Beliefs beliefs, Desires desires) {

        Desire goal = desires.getDesire(DesireType.GOAL_LOCATION);

        EntityID position = ((Human)getAgent().me()).getPosition();

        List<EntityID> steps = breadthFirstSearch(beliefs,desires,position,goal.getEntityID());

        return steps;
    }

    public List<EntityID> createPlan(Beliefs beliefs, Desires desires,EntityID position) {

        Desire goal = desires.getDesire(DesireType.GOAL_LOCATION);

       // EntityID position = ((Human)getAgent().me()).getPosition();

        List<EntityID> steps = breadthFirstSearch(beliefs,desires,position,goal.getEntityID());

        return steps;
    }


    public void setGraph(Map<EntityID, Set<EntityID>> newGraph) {
        this.graph = newGraph;
    }

    public Map<EntityID, Set<EntityID>> getGraph() {
        return graph;
    }

    private List<EntityID> breadthFirstSearch(Beliefs beliefs, Desires desires,EntityID start, EntityID... goals) {
        return breadthFirstSearch(beliefs,desires,start, Arrays.asList(goals));
    }

    private List<EntityID> breadthFirstSearch(Beliefs beliefs, Desires desires,EntityID start, Collection<EntityID> goals) {

        List<EntityID> open = new LinkedList<EntityID>();
        Map<EntityID, EntityID> ancestors = new HashMap<EntityID, EntityID>();
        open.add(start);
        EntityID next = null;
        boolean found = false;
        ancestors.put(start, start);
        do {
            next = open.remove(0);
            if (isGoal(next, goals)) {
                found = true;
                desires.removeDesire(DesireType.GOAL_LOCATION);
                break;
            }
            Collection<EntityID> neighbours = graph.get(next);
            if (neighbours.isEmpty()) {
                continue;
            }
            for (EntityID neighbour : neighbours) {
                if (isGoal(neighbour, goals)) {
                    ancestors.put(neighbour, next);
                    next = neighbour;
                    found = true;
                    break;
                } else {
                    if (!ancestors.containsKey(neighbour)) {
                        open.add(neighbour);
                        ancestors.put(neighbour, next);
                    }
                }
            }
        } while (!found && !open.isEmpty());
        if (!found) {
            // No path
            return null;
        }
        // Walk back from goal to start
        EntityID current = next;
        List<EntityID> path = new LinkedList<EntityID>();
        do {
            path.add(0, current);
            current = ancestors.get(current);
            if (current == null) {
                throw new RuntimeException("Found a node with no ancestor! Something is broken.");
            }
        } while (current != start);
        return path;
    }

    private boolean isGoal(EntityID e, Collection<EntityID> test) {
        return test.contains(e);
    }
}
