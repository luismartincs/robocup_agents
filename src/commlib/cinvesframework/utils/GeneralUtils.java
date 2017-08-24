package commlib.cinvesframework.utils;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.belief.EntityListBelief;
import implementation.agents.Quadrant;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import sample.DistanceSorter;

import java.util.*;

@SuppressWarnings("Duplicates")
public class GeneralUtils {



    /**
     * Check Civilians Around
     */

    public static ArrayList<Human> getHumanTargets(CinvesAgent agent,ChangeSet changeSet){

        StandardEntity entity;
        StandardWorldModel model = agent.getWorldModel();
        ArrayList<Human> civilians = new ArrayList<>();

        for(EntityID id : changeSet.getChangedEntities()){
            entity = model.getEntity(id);

            if(entity instanceof Human && agent.getID().getValue() != id.getValue()){

                Human human = (Human) entity;

                if (human.isHPDefined() && human.isBuriednessDefined()
                        && human.isDamageDefined() && human.isPositionDefined()
                        && human.getHP() >= 0
                        && (human.getBuriedness() >= 0 || human.getDamage() >= 0)) {

                    civilians.add(human);
                }

                //civilians.add(human);

            }
        }

        StandardEntity position = ((Human)agent.me()).getPosition(agent.getWorldModel());

        Collections.sort(civilians, new DistanceSorter(position, model));

        return civilians;
    }

    public static ArrayList<Building> getBurningBuildings(CinvesAgent agent, ChangeSet changeSet) {
        StandardWorldModel model = agent.getWorldModel();
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        ArrayList<Building> result = new ArrayList<Building>();

        StandardEntity entity;
        for(EntityID id : changeSet.getChangedEntities()) {
            entity = model.getEntity(id);
            if (entity instanceof Building) {
                Building b = (Building)entity;
                if (b.isOnFire()) {
                    result.add(b);
                }
            }
        }

        /*for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (b.isOnFire()) {
                    result.add(b);
                }
            }
        }
        Así es como lo hacen en los samples.
        */
        StandardEntity position = ((Human)agent.me()).getPosition(agent.getWorldModel());
        // Sort by distance
        Collections.sort(result, new DistanceSorter(position, model) );
        return result;
    }


    public static void updateBuildingsInQuadrant(Beliefs beliefs,StandardWorldModel model,int quadrant){
        if (beliefs.getBelief(BeliefType.BUILDINGS_IN_QUADRANT) == null) {

            EntityListBelief buildingsInQuadrant = new EntityListBelief();
            EntityListBelief buildings = (EntityListBelief) beliefs.getBelief(BeliefType.BUILDINGS);

            for (StandardEntity building : buildings.getEntities()) {

                Pair<Integer, Integer> point = building.getLocation(model);

                int px = point.first();
                int py = point.second();
                int q = Quadrant.getQuadrant(model, px, py);

                if (q == quadrant) {
                    buildingsInQuadrant.addEntity(building);
                }

            }
            beliefs.addBelief(BeliefType.BUILDINGS_IN_QUADRANT, buildingsInQuadrant);
        }
    }

    /**
     * Check Civilians Around
     */

    public static ArrayList<Civilian> getCivilianAround(CinvesAgent agent,ChangeSet changeSet){

        StandardEntity entity;
        StandardWorldModel model = agent.getWorldModel();
        ArrayList<Civilian> civilians = new ArrayList<>();

        for(EntityID id : changeSet.getChangedEntities()){

            entity = model.getEntity(id);

            if(entity instanceof Civilian){
                civilians.add((Civilian)entity);
            }
        }

        return civilians;
    }



    /**
     * Check PoliceForce
     */

    public static ArrayList<PoliceForce> getPoliceForceAround(CinvesAgent agent,ChangeSet changeSet){

        StandardEntity entity;
        StandardWorldModel model = agent.getWorldModel();
        ArrayList<PoliceForce> policeForces = new ArrayList<>();

        for(EntityID id : changeSet.getChangedEntities()){

            entity = model.getEntity(id);

            if(entity instanceof PoliceForce){
                if(entity.getID().getValue() != agent.getID().getValue()) {
                    policeForces.add((PoliceForce) entity);
                }
            }
        }

        return policeForces;
    }

    /**
     * Check Blockades
     */
    public static Blockade getTargetBlockade(int distance, CinvesAgent agent){
       // int distance = config.getIntValue("clear.repair.distance");

        if (agent.location() instanceof Area) {

            Area location = (Area) agent.location();
            Blockade result = getTargetBlockade(agent, location, distance);
            if (result != null) {
                return result;
            }

            for (EntityID next : location.getNeighbours()) {
                location = (Area) agent.getWorldModel().getEntity(next);
                result = getTargetBlockade(agent, location, distance);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static Road getRoad( int distance, CinvesAgent agent){
        // int distance = config.getIntValue("clear.repair.distance");
        try {
            Road road = null;
            Human human = ((Human) agent.me());
            int x = human.getX();
            int y = human.getY();
            double minDist = Double.MAX_VALUE;
            HashSet<StandardEntity> ids = (HashSet) agent.getWorldModel().getEntitiesOfType(StandardEntityURN.ROAD);
            for (StandardEntity se : ids) {
                    if (((Road) se).getID() != agent.location().getID()) {
                        if (!agent.targetRoads.contains(se.getID())) {
                            double dist = euclidianDistance(x, y, ((Road) se).getX(), ((Road) se).getY());
                            if (dist < minDist) {
                               minDist = dist;
                               road= (Road) se;
                            }
                        }

                    }
            }
            agent.targetRoads.add(road.getID());
            return road;
        }catch(Exception ex){
            return null;
        }
    }

    private static double euclidianDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }

    private static Blockade getTargetBlockade(CinvesAgent agent,Area area, int maxDistance){
        // Logger.debug("Looking for nearest blockade in " + area);
        Human human = ((Human)agent.me());

        if(area == null || !area.isBlockadesDefined()){
            // Logger.debug("Blockades undefined");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = human.getX();
        int y = human.getY();
        for(EntityID next : ids){
            Blockade b = (Blockade) agent.getWorldModel().getEntity(next);
            double d = findDistanceTo(b, x, y);
            // Logger.debug("Distance to " + b + " = " + d);
            if(maxDistance < 0 || d < maxDistance){
                // Logger.debug("In range");
                return b;
            }
        }
        // Logger.debug("No blockades in range");
        return null;
    }




    private static int findDistanceTo(Blockade b, int x, int y){
        // Logger.debug("Finding distance to " + b + " from " + x + ", " + y);
        List<Line2D> lines = GeometryTools2D.pointsToLines(
                GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y);
        for(Line2D next : lines){
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            // Logger.debug("Next line: " + next + ", closest point: " + closest +
            // ", distance: " + d);
            if(d < best){
                best = d;
                // Logger.debug("New best distance");
            }

        }
        return (int) best;
    }

}
