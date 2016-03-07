package util.persistence;

import jdk.nashorn.internal.ir.ObjectNode;
import model.*;

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
