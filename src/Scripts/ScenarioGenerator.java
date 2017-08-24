package Scripts;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


public class ScenarioGenerator {
    private JTextField policeBuilding;
    String pathSave;
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
    ArrayList<Integer> buildings;
    ArrayList<Integer> roads;
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
        File xmlFile = new File(pathSave+"map.gml");

        try {

            Document document = builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            for(Element at:rootNode.getChildren()){
                if(at.getName().equals("buildinglist")){
                    for(Element e:at.getChildren()){
                        buildings.add(e.getAttributes().get(0).getIntValue());
                    }
                }
                if(at.getName().equals("roadlist")){
                    for(Element e:at.getChildren()){
                        roads.add(e.getAttributes().get(0).getIntValue());
                    }
                }
            }
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }
    }

    public void generateLists(int policeForceB, int policeForceR, int ambulancesB, int ambulancesR, int firebB, int firebR, int civiliansB,
                                   int civiliansR, int pOffices, int refuges, int fires, int firecentrals, int ambulancecentrals, int hydrants){
        for(int i=0;i<policeForceB;i++){
            int rnd=(int)(Math.random()*buildings.size());
            polices.add(buildings.get(rnd));
        }
        for(int i=0;i<policeForceR;i++){
            int rnd=(int)(Math.random()*roads.size());
            polices.add(roads.get(rnd));
        }
        for(int i=0;i<ambulancesB;i++){
            int rnd=(int)(Math.random()*buildings.size());
            ambulances.add(buildings.get(rnd));
        }
        for(int i=0;i<ambulancesR;i++){
            int rnd=(int)(Math.random()*roads.size());
            ambulances.add(roads.get(rnd));
        }
        for(int i=0;i<firebB;i++){
            int rnd=(int)(Math.random()*buildings.size());
            fireBrigades.add(buildings.get(rnd));
        }
        for(int i=0;i<firebR;i++){
            int rnd=(int)(Math.random()*roads.size());
            fireBrigades.add(roads.get(rnd));
        }
        for(int i=0;i<civiliansB;i++){
            int rnd=(int)(Math.random()*buildings.size());
            civilians.add(buildings.get(rnd));
        }
        for(int i=0;i<civiliansR;i++){
            int rnd=(int)(Math.random()*roads.size());
            civilians.add(roads.get(rnd));
        }
        for(int i=0;i<pOffices;i++){
            int rnd=(int)(Math.random()*buildings.size());
            pOfficesList.add(buildings.get(rnd));
        }
        for(int i=0;i<refuges;i++){
            int rnd=(int)(Math.random()*buildings.size());
            refugeList.add(buildings.get(rnd));
        }
        for(int i=0;i<fires;i++){
            int rnd=(int)(Math.random()*buildings.size());
            fireList.add(buildings.get(rnd));
        }
        for(int i=0;i<firecentrals;i++){
            int rnd=(int)(Math.random()*buildings.size());
            fireCentralsList.add(buildings.get(rnd));
        }
        for(int i=0;i<ambulancecentrals;i++){
            int rnd=(int)(Math.random()*buildings.size());
            ambCentralsList.add(buildings.get(rnd));
        }
        for(int i=0;i<hydrants;i++){
            int rnd=(int)(Math.random()*roads.size());
            hydrantList.add(roads.get(rnd));
        }
        doFile();


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
