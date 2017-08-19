package implementation.agents;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;

import java.io.*;
import java.nio.file.Files;

public class Quadrant {

    public static int divX=-1;
    public static int divY=-1;
    public static int width=0; //468520;
    public static int height=0; //364080;
    public static int quadrants[][];

    /**
     * obtain the x quadrant
     * @param x
     * @return
     */
    public static int qx(int x){
        int div=width/divX;
        return (x/div);
    }

    /**
     * obtain the y quadrant
     * @param y
     * @return
     */
    public static int qy(int y){
        int div=height/divY;
        return (y/div);
    }

    /**
     * make the quadrants, first the file is readed
     */
    public static void makeQuadrants(){
        loadQuadrants();
        quadrants=new int[divX][divY];
        int c=0;
        for(int i=0;i<divX;i++){
            for(int j=0;j<divY;j++){
                quadrants[i][j]=c;
                c++;
            }
        }
    }

    /**
     * get the quadrants by the position of the agent
     * @param model
     * @param x
     * @param y
     * @return
     */
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

    /**
     * read a file
     * @param file
     * @return
     */
    public static String readFile(File file) {
        String content = "";
        try (InputStream in = Files.newInputStream(file.toPath());
             BufferedReader reader
                     = new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                content = content + line + "\n";
            }
        } catch (IOException x) {
            System.err.println("Unable to read file quadrants.txt");
        }
        return content;
    }

    /**
     * load the quadrant information by the file located in
     * /home/quadrants.txt
     */
    public static void loadQuadrants(){
        String str=readFile(new File("/home/quadrants.txt"));
        String[] array=str.split("\n");
        divX=Integer.parseInt(array[2]);
        divY=Integer.parseInt(array[3]);
    }

}
