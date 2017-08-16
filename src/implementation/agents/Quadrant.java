package implementation.agents;

import commlib.cinvesframework.agent.CinvesAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.Entity;

public class Quadrant {

    public static int divX=2;
    public static int divY=2;
    public static int width=450000;
    public static int height=360000;
    public static int quadrants[][];

    public static int qx(int x){
        int div=width/divX;
        return (x/div);
    }

    public static int qy(int y){
        int div=height/divY;
        return (y/div);
    }

    public static void makeQuadrants(){
        quadrants=new int[divX][divY];
        int c=0;
        for(int i=0;i<divX;i++){
            for(int j=0;j<divY;j++){
                quadrants[i][j]=c;
                c++;
            }
        }
    }

    public static int getQuadrant(int x, int y){
        if(quadrants==null){
            makeQuadrants();
        }
        return quadrants[qx(x)][qy(y)];
    }

}
