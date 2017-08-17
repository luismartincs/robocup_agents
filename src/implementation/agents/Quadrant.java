package implementation.agents;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;

public class Quadrant {

    public static int divX=2;
    public static int divY=2;
    public static int width=0; //450000;
    public static int height=0; //360000;
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

    public static int getQuadrant(StandardWorldModel model, int x, int y){
        if(quadrants==null){

            Pair<Integer, Integer> min = model.getWorldBounds().first();
            Pair<Integer, Integer> max = model.getWorldBounds().second();

            width = max.first();
            height = max.second();

            makeQuadrants();
        }
        return quadrants[qx(x)][qy(y)];
    }

}
