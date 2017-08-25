package commlib.cinvesframework.belief;
import java.io.FileWriter;
import java.io.IOException;

import implementation.agents.Quadrant;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;

public class SaveBeliefs {

    static boolean block=false;

    public static void saveBeliefs(EntityListBelief buildings, EntityListBelief roads, StandardWorldModel model){
        if(!block) {
            try {

                Element beliefs = new Element("beliefs");
                Document doc = new Document();
                doc.setRootElement(beliefs);
                beliefs.setAttribute(new Attribute("quadrants",""+Quadrant.divX*Quadrant.divY));

                Element buildingsElement = new Element("buildings");
                Element roadsElement = new Element("roads");

                for (StandardEntity en : buildings.getEntities()) {
                    Building b = (Building) en;
                    Element elm=new Element("building");
                    elm.setAttribute(new Attribute("id",""+b.getID()));
                    elm.setAttribute(new Attribute("quadrant",""+Quadrant.getQuadrant(model, b.getX(), b.getY())));
                    buildingsElement.addContent(elm);
                }

                for (StandardEntity en : roads.getEntities()) {
                    Road b = (Road) en;
                    Element elm=new Element("road");
                    elm.setAttribute(new Attribute("id",""+b.getID()));
                    elm.setAttribute(new Attribute("quadrant",""+Quadrant.getQuadrant(model, b.getX(), b.getY())));
                    roadsElement.addContent(elm);
                }
                doc.getRootElement().addContent(buildingsElement);
                doc.getRootElement().addContent(roadsElement);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter("beliefs.xml"));

                System.out.println("File Saved!");
                block=true;
            } catch (IOException io) {
                System.out.println(io.getMessage());
            }
        }
    }


}

