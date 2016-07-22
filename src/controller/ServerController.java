package controller;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.gson.Gson;
import controller.MainController;
import model.*;
import util.Constants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by Marcus on 2016-07-21.
 */
public class ServerController implements PropertyChangeListener {

    private Graph graph;
    private Server server;
    private MainController mainController;

    public ServerController(Graph pGraph, MainController pMainController) {
        mainController = pMainController;
        graph = pGraph;
        graph.addPropertyChangeListener(this);

        server = new Server();
        server.start();
        try {
            server.bind(54555,54777);
        } catch (IOException e){
            e.printStackTrace();
        }

        Kryo kryo = server.getKryo();
        kryo.register(ClassNode.class);
        kryo.register(AbstractNode.class);
        kryo.register(PackageNode.class);
        kryo.register(AbstractEdge.class);
        kryo.register(InheritanceEdge.class);
        kryo.register(CompositionEdge.class);
        kryo.register(AssociationEdge.class);
        kryo.register(AggregationEdge.class);
        kryo.register(Graph.class);
        kryo.register(ArrayList.class);
        kryo.register(model.AbstractEdge.Direction.class);
        kryo.register(String[].class);

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof String) {
                    String request = (String)object;
                    if(request.equals("RequestGraph")){
                        connection.sendTCP(mainController.getGraphModel());
                    }
                }
            }
        });
    }

    /**
     * Handles property changes in the model and forwards them to all connected clients.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("PropertyChange: " + evt.getPropertyName());
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.NodeAdd)) {
            AbstractNode node = (AbstractNode) evt.getNewValue();
            server.sendToAllTCP(node);
        }
        else if (propertyName.equals(Constants.NodeRemove)){
            String[] dataArray = {Constants.NodeRemove, (String)evt.getNewValue()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.EdgeAdd)){
            AbstractEdge edge = (AbstractEdge) evt.getNewValue();
            edge.addPropertyChangeListener(this);
            server.sendToAllTCP(edge);
        }
        else if(propertyName.equals(Constants.EdgeRemove)) {
            String [] dataArray = {Constants.EdgeRemove, (String)evt.getNewValue()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTranslateX) || propertyName.equals(Constants.changeNodeTranslateY)) { //NodeX/Y not needed
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), Double.toString(node.getTranslateX()), Double.toString(node.getTranslateY())};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeWidth) || propertyName.equals(Constants.changeNodeHeight)){
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), Double.toString(node.getWidth()), Double.toString(node.getHeight())};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTitle)) {
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), node.getTitle()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeClassNodeAttributes) || propertyName.equals(Constants.changeClassNodeOperations)){
            ClassNode node = (ClassNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), node.getAttributes(), node.getOperations()};
            server.sendToAllTCP(dataArray);
        }
        else if(propertyName.equals(Constants.changeEdgeStartMultiplicity) || propertyName.equals(Constants.changeEdgeEndMultiplicity)){
            AbstractEdge edge = (AbstractEdge) evt.getSource();
            String[] dataArray = {propertyName, edge.getId(), edge.getStartMultiplicity(), edge.getEndMultiplicity()};
            server.sendToAllTCP(dataArray);
        }
    }
}
