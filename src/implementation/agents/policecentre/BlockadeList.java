package implementation.agents.policecentre;

import rescuecore2.standard.entities.Blockade;

import java.util.HashSet;

public class BlockadeList {

    HashSet<BlockadeInfo> blockades;

    public BlockadeList(){
        blockades=new HashSet<>();
    }

    public void addBlockade(BlockadeInfo bi){
        blockades.add(bi);

        System.out.println(blockades.size());
    }

    public void addBlockade(Blockade bl){
        blockades.add(new BlockadeInfo(bl.getX(),bl.getY(),bl.getRepairCost(),bl.getID().getValue()));
    }
}
