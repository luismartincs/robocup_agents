package Scripts;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


public class ScenarioGenerator {
    private JTextField policeBuilding;
    String pathSave;
    int quadrants;
    private JTextField policeRoad;
    private JButton createButton;
    private JTextField ambulancesBuildings;
    private JTextField abulancesRoadsTextField;
    private JTextField fireBrigadeBuildings;
    private JTextField fireBrigadeRoads;
    private JTextField civiliansBuilding;
    private JTextField civiliansRoad;
    private JTextField policeOffices;
    private JTextField refuges;
    private JTextField fires;
    private JPanel Panel;
    private JTextField fireCentrals;
    private JTextField ambCentrals;
    private JTextField Hydrants;
    ArrayList<Entity> buildings;
    ArrayList<Entity> roads;
    ArrayList<Integer> civilians;
    ArrayList<Integer> polices;
    ArrayList<Integer> fireBrigades;
    ArrayList<Integer> ambulances;
    ArrayList<Integer> pOfficesList;
    ArrayList<Integer> fireList;
    ArrayList<Integer> refugeList;
    ArrayList<Integer> fireCentralsList;
    ArrayList<Integer> ambCentralsList;
    ArrayList<Integer> hydrantList;

    public ScenarioGenerator() {
        buildings=new ArrayList<>();
        roads=new ArrayList<>();
        civilians=new ArrayList<>();
        polices=new ArrayList<>();
        fireBrigades=new ArrayList<>();
        ambulances=new ArrayList<>();
        pOfficesList=new ArrayList<>();
        fireList=new ArrayList<>();
        refugeList=new ArrayList<>();
        fireCentralsList=new ArrayList<>();
        ambCentralsList=new ArrayList<>();
        hydrantList=new ArrayList<>();
        loadConfig();
        loadXml();
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                civilians.clear();
                polices.clear();
                fireBrigades.clear();
                ambulances.clear();
                pOfficesList.clear();
                fireList.clear();
                refugeList.clear();
                fireCentralsList.clear();
                ambCentralsList.clear();
                hydrantList.clear();
                makeConfigFile();
                generateLists(val(policeBuilding), val(policeRoad),val(ambulancesBuildings),val(abulancesRoadsTextField),val(fireBrigadeBuildings),
                        val(fireBrigadeRoads),val(civiliansBuilding),val(civiliansRoad),val(policeOffices),val(refuges),val(fires), val(fireCentrals)
                ,val(ambCentrals),val(Hydrants));
            }
        });
    }

    public void setData(ScenarioGenerator data) {
    }

    public void getData(ScenarioGenerator data) {
    }

    public boolean isModified(ScenarioGenerator data) {
        return false;
    }

    public static void main(String [] args){
        JFrame frame=new JFrame("ScenarioGenerator");
        frame.setContentPane(new ScenarioGenerator().Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Load a xml file
     */
    public void loadXml()
    {
        buildings.clear();
        roads.clear();
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File("beliefs.xml");

        try {

            Document document = builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            quadrants=rootNode.getAttribute("quadrants").getIntValue();
            for(Element e:rootNode.getChild("buildings").getChildren()){
                buildings.add(new Entity(e.getAttribute("id").getIntValue(),e.getAttribute("quadrant").getIntValue()));
            }
            for(Element e:rootNode.getChild("roads").getChildren()){
                roads.add(new Entity(e.getAttribute("id").getIntValue(),e.getAttribute("quadrant").getIntValue()));
            }
            System.out.println(quadrants+" "+buildings.size()+" "+roads.size());

        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }
    }

    public void generateLists(int policeForceB, int policeForceR, int ambulancesB, int ambulancesR, int firebB, int firebR, int civiliansB,
                                   int civiliansR, int pOffices, int refuges, int fires, int firecentrals, int ambulancecentrals, int hydrants){
        /*
        CREATE N NUMBER OF ARRAYLIST WHERE N IS THE NUMBER OF THE QUADRANTS
         */
        ArrayList<Integer>[] buildingQuadrants=new ArrayList[quadrants];
        ArrayList<Integer>[] roadQuadrants=new ArrayList[quadrants];
        for(int i=0;i<quadrants;i++){
            buildingQuadrants[i]=new ArrayList<>();
            roadQuadrants[i]=new ArrayList<>();
        }
        for(Entity en:buildings){
            buildingQuadrants[en.getQuadrant()].add(en.getId());
        }
        for(Entity en:roads){
            roadQuadrants[en.getQuadrant()].add(en.getId());
        }
        /*
        -------------------------------------------------------------------------------------------------
         */
        civilians=addEntities(buildingQuadrants, roadQuadrants, civiliansB, civiliansR);
        polices=addEntities(buildingQuadrants, roadQuadrants, policeForceB  , policeForceR);
        ambulances=addEntities(buildingQuadrants, roadQuadrants, ambulancesB  , ambulancesR);
        fireBrigades=addEntities(buildingQuadrants, roadQuadrants, firebB  , firebR);
        /*
        -------------------------------------------------------------------------------------------------
         */
        for(int i=0;i<pOffices;i++){
            int rnd=(int)(Math.random()*buildings.size());
            pOfficesList.add(buildings.get(rnd).getId());
        }
        for(int i=0;i<refuges;i++){
            int rnd=(int)(Math.random()*buildings.size());
            refugeList.add(buildings.get(rnd).getId());
        }
        for(int i=0;i<fires;i++){
            int rnd=(int)(Math.random()*buildings.size());
            fireList.add(buildings.get(rnd).getId());
        }
        for(int i=0;i<firecentrals;i++){
            int rnd=(int)(Math.random()*buildings.size());
            fireCentralsList.add(buildings.get(rnd).getId());
        }
        for(int i=0;i<ambulancecentrals;i++){
            int rnd=(int)(Math.random()*buildings.size());
            ambCentralsList.add(buildings.get(rnd).getId());
        }
        for(int i=0;i<hydrants;i++){
            int rnd=(int)(Math.random()*roads.size());
            hydrantList.add(roads.get(rnd).getId());
        }
        doFile();


    }

    public ArrayList<Integer> addEntities(ArrayList<Integer>[] buildings, ArrayList<Integer>[] roads, int s1, int s2){
        ArrayList<Integer> humans=new ArrayList<>();
        for(int i=0;i<quadrants;i++){
            int size=buildings[i].size();
            for(int j=0;j<s1;j++){
                int rnd=(int)(Math.random()*size);
                humans.add(buildings[i].get(rnd));
            }
            int size2=roads[i].size();
            for(int j=0;j<s2;j++){
                int rnd=(int)(Math.random()*size2);
                humans.add(roads[i].get(rnd));
            }
        }
        return humans;
    }

    public void doFile(){
        String cad="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        cad=cad+"<scenario:scenario xmlns:scenario=\"urn:roborescue:map:scenario\">\n";
        for(int id:fireList){
            cad=cad+"<scenario:fire scenario:location=\""+id+"\"/>\n";
        }
        for(int id:fireCentralsList){
            cad=cad+"<scenario:firestation scenario:location=\""+id+"\"/>\n";
        }
        for(int id:hydrantList){
            cad=cad+"<scenario:hydrant scenario:location=\""+id+"\"/>\n";
        }
        for(int id:ambCentralsList){
            cad=cad+"<scenario:ambulancecentre scenario:location=\""+id+"\"/>\n";
        }
        for(int id:refugeList){
            cad=cad+"<scenario:refuge scenario:location=\""+id+"\"/>\n";
        }
        for(int id:pOfficesList){
            cad=cad+"<scenario:policeoffice scenario:location=\""+id+"\"/>\n";
        }
        for(int id:civilians){
            cad=cad+"<scenario:civilian scenario:location=\""+id+"\"/>\n";
        }
        for(int id:polices){
            cad=cad+"<scenario:policeforce scenario:location=\""+id+"\"/>\n";
        }
        for(int id:fireBrigades){
            cad=cad+"<scenario:firebrigade scenario:location=\""+id+"\"/>\n";
        }
        for(int id:ambulances){
            cad=cad+"<scenario:ambulanceteam scenario:location=\""+id+"\"/>\n";
        }
        cad=cad+"</scenario:scenario>";
        write(pathSave+"scenario",cad,"xml");
    }

    public int val(JTextField field){
        return Integer.parseInt(field.getText());
    }

    /**
     * write a file
     *
     * @param name
     * @param cont
     */
    public static void write(String name, String cont, String ext) {
        FileWriter fichero = null;
        BufferedWriter pw = null;
        try {
            fichero = new FileWriter(name + "."+ext);
            pw = new BufferedWriter(fichero);
            pw.write(cont);
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fichero) {
                    fichero.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
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

    public void loadConfig(){
        String str=readFile(new File("configScript.txt"));
        String[] array=str.split("\n");
        pathSave=array[0];
        policeBuilding.setText(array[1]);
        policeRoad.setText(array[2]);
        ambulancesBuildings.setText(array[3]);
        abulancesRoadsTextField.setText(array[4]);
        fireBrigadeBuildings.setText(array[5]);
        fireBrigadeRoads.setText(array[6]);
        civiliansBuilding.setText(array[7]);
        civiliansRoad.setText(array[8]);
        policeOffices.setText(array[9]);
        refuges.setText(array[10]);
        fires.setText(array[11]);
        fireCentrals.setText(array[12]);
        ambCentrals.setText(array[13]);
        Hydrants.setText(array[14]);
    }

    public void makeConfigFile(){
        String c=pathSave+"\n";
        c=c+policeBuilding.getText()+"\n";
        c=c+policeRoad.getText()+"\n";
        c=c+ambulancesBuildings.getText()+"\n";
        c=c+abulancesRoadsTextField.getText()+"\n";
        c=c+fireBrigadeBuildings.getText()+"\n";
        c=c+fireBrigadeRoads.getText()+"\n";
        c=c+civiliansBuilding.getText()+"\n";
        c=c+civiliansRoad.getText()+"\n";
        c=c+policeOffices.getText()+"\n";
        c=c+refuges.getText()+"\n";
        c=c+fires.getText()+"\n";
        c=c+fireCentrals.getText()+"\n";
        c=c+ambCentrals.getText()+"\n";
        c=c+Hydrants.getText()+"";
        write("configScript",c,"txt");
    }

}
class Entity{
    int id;
    int quadrant;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Entity(int id, int quadrant) {
        this.id = id;
        this.quadrant = quadrant;
    }

    public int getQuadrant() {

        return quadrant;
    }

    public void setQuadrant(int quadrant) {
        this.quadrant = quadrant;
    }
}
