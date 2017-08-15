package implementation.agents.policecentre;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.intention.SearchPlan;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BlockadeList {

    HashSet<BlockadeInfo> blockades;

    public BlockadeList(){
        blockades=new HashSet<>();
    }

    /**
     * add a blockade
     * @param bi
     */
    public void addBlockade(BlockadeInfo bi){
        if(blockades.add(bi)){
            System.out.println("agregado blockade "+bi.getId());
        }
    }

    public void addBlockade(Blockade bl){
        if(blockades.add(new BlockadeInfo(bl))){
            System.out.println("agregado blockade "+bl.getID().getValue());
        }
    }

    /**
     * remove a blockade by the id
     * @param blockadeID
     */
    public void removeBlockade(EntityID blockadeID){
        BlockadeInfo bi=getBlockadeByID(blockadeID);
        if(bi!=null){
            blockades.remove(bi);
            System.out.println("removido blockade "+bi.getId());
        }
    }

    /**
     * search a blockade by the id
     * @param id
     * @return
     */
    public BlockadeInfo getBlockadeByID(EntityID id){
        for(BlockadeInfo bi:blockades){
            if(bi.getId()==id.getValue()){
                return bi;
            }
        }
        return null;
    }

    /**
     * get nearest distance using euclidian distance
     * @param id
     * @param xPosition
     * @param yPosition
     * @return
     */
    public BlockadeInfo getNearestBlockeade( EntityID id, int xPosition, int yPosition){
        double minDist=Double.MAX_VALUE;
        BlockadeInfo blockade=null;
        for(BlockadeInfo binfo: blockades){
            double distance=getDistance(binfo.xPosition,binfo.yPosition,xPosition,yPosition);
            if(distance<minDist){
                minDist=distance;
                blockade=binfo;
            }
        }
        System.out.println("distance:       "+minDist);
        return blockade;
    }

    /**
     * calculate the euclidian distance between 2 points
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    double getDistance(int x1, int y1, int x2, int y2){
        int diffX=(x2-x1);
        int diffY=(y2-y1);
        return Math.sqrt(Math.pow(diffX,2)+Math.pow(diffY,2));
    }
}
