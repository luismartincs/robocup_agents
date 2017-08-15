package implementation.agents.policecentre;

import commlib.information.WorldInformation;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.worldmodel.EntityID;

import java.util.Comparator;

public class BlockadeInfo implements Comparator<BlockadeInfo>{

    Blockade blockade;
    int xPosition;
    int yPosition;
    int repairCost;
    int id;
    int unknowCost=10000;


    public BlockadeInfo(int xPosition, int yPosition, int repairCost, int id) {
        this.xPosition=xPosition;
        this.yPosition=yPosition;
        this.repairCost=repairCost;
        this.id=id;
        blockade=new Blockade(new EntityID(id));
        blockade.setX(xPosition);
        blockade.setY(yPosition);
        blockade.setRepairCost(repairCost);
        blockade.setPosition(new EntityID(id));
    }

    public BlockadeInfo(int xPosition, int yPosition, int id) {
        this.xPosition=xPosition;
        this.yPosition=yPosition;
        this.repairCost=unknowCost;
        this.id=id;
        blockade=new Blockade(new EntityID(id));
        blockade.setX(xPosition);
        blockade.setY(yPosition);
        blockade.setRepairCost(unknowCost);
        blockade.setPosition(new EntityID(id));
    }

    public BlockadeInfo(Blockade blockade){
        this.blockade=blockade;
        System.out.println(blockade.getX());
        xPosition=blockade.getX();
        yPosition=blockade.getY();
        repairCost=blockade.getRepairCost();
        id=blockade.getID().getValue();
    }

    public Blockade getBlockade() {
        return blockade;
    }

    public void setBlockade(Blockade blockade) {
        this.blockade = blockade;
    }

    public int getxPosition() {
        return xPosition;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(int repairCost) {
        this.repairCost = repairCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode(){
        return id;
    }

    @Override
    public boolean equals(Object arg0) {
        return this.getId()==((BlockadeInfo)arg0).getId();
    }



    @Override
    public int compare(BlockadeInfo blockadeInfo, BlockadeInfo t1) {
        if(blockadeInfo.getRepairCost()>t1.getRepairCost()){
            return -1;
        }
        if(blockadeInfo.getRepairCost()<t1.getRepairCost()){
            return 1;
        }
        return 0;

    }
}
