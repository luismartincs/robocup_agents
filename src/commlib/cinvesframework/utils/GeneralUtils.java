package commlib.cinvesframework.utils;

import commlib.cinvesframework.agent.CinvesAgent;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;

import java.util.List;

public class GeneralUtils {


    public static Blockade getTargetBlockade(int distance, CinvesAgent agent){
       // int distance = config.getIntValue("clear.repair.distance");

        Area location = (Area) agent.location();
        Blockade result = getTargetBlockade(agent,location, distance);
        if(result != null){
            return result;
        }

        for(EntityID next : location.getNeighbours()){
            location = (Area) agent.getWorldModel().getEntity(next);
            result = getTargetBlockade(agent,location, distance);
            if(result != null){
                return result;
            }
        }
        return null;
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
