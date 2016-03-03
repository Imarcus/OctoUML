package controller;

import jdk.nashorn.internal.ir.ObjectNode;
import model.AbstractNode;
import model.Graph;
import model.PackageNode;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Marcus on 2016-03-03.
 */
public class PersistenceController {

    public static void saveFile(Graph pGraph)
    {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("mysave.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        XMLEncoder encoder = new XMLEncoder(bos);
        for(AbstractNode node : pGraph.getAllNodes()){
            encoder.writeObject(node);
        }
        encoder.close();
    }
}
