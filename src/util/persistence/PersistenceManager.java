package util.persistence;

import jdk.nashorn.internal.ir.ObjectNode;
import model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcus on 2016-03-03.
 */
public class PersistenceManager {

    public static void saveFile(Graph pGraph, String path)
    {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        XMLEncoder encoder = new XMLEncoder(bos);
        encoder.writeObject(pGraph);
        /*for(AbstractNode node : pGraph.getAllNodes()){
            encoder.writeObject(node);
        }
        for(Edge edge : pGraph.getAllEdges()){
            encoder.writeObject(edge);
        }*/

        encoder.close();
    }

    public static void exportXMI(Graph pGraph, String path){
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("XMI");
            rootElement.setAttribute("xmi.version", "1.2");
            rootElement.setAttribute("xmlns:UML", "org.omg/UML/1.4");
            doc.appendChild(rootElement);

            Element xmiHeader = doc.createElement("XMI.header");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            transformer.transform(source, result);

            System.out.println("File saved!");


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }

    public static Graph loadFile(String path){

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedInputStream bis = new BufferedInputStream(fis);
        XMLDecoder decoder = new XMLDecoder(bis);
        Graph graph = (Graph) decoder.readObject();
        return graph;
    }
}
