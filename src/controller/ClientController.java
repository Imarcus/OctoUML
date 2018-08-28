package controller;

import com.esotericsoftware.kryo.Kryo;


import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import model.*;
import model.edges.*;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.PackageNode;
import util.Constants;
import util.GlobalVariables;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Communicates changes in the graph to and from the host.
 * Used by MainController when connecting to a host.
 */
public class ClientController implements PropertyChangeListener {

	private static Logger logger = LoggerFactory.getLogger(ClientController.class);
    private AbstractDiagramController diagramController;
    private Client client;
    private String serverIp;
    private int port;

    public ClientController(AbstractDiagramController pDiagramController, String pServerIp, int pPort) {
    	logger.debug("ClientController()");
        diagramController = pDiagramController;
        serverIp = pServerIp;
        port = pPort;
        
        // Increase of buffers size to avoid overflow. Defaults: 8192, 2048
        client = new Client(16384,16384);

        initKryo(client.getKryo());

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
            	if ( object instanceof Object[]) {
                	logger.debug("received()");
                	Object[] dataArray = (Object[]) object; 
                    if ( dataArray[0] instanceof AbstractNode) {
                        Platform.runLater(() -> diagramController.createNodeView((AbstractNode)dataArray[0], true));
                    } 
                    else if (dataArray[0] instanceof AbstractEdge) {
                        Platform.runLater(() -> diagramController.addEdgeView((AbstractEdge)dataArray[0], true));
                    }
                    else if (dataArray[0] instanceof Graph){
                        Graph graph = (Graph) dataArray[0];
                        graph.addRemotePropertyChangeListener(ClientController.this);
                        Platform.runLater(() -> diagramController.load(graph, true));
                    	GlobalVariables.setCollaborationType((String)dataArray[1]);
                    	logger.info("Collaboration type setted to " + dataArray[1]);
                        Platform.runLater(() -> diagramController.setServerLabel("User: "+ GlobalVariables.getUserName() +
                        		"\nCollaboration type: " + GlobalVariables.getCollaborationType() +
                        		"\nClient mode"));
                    	
                    }
                    else if (dataArray[0] instanceof String){
                    	String request = (String) dataArray[0];
                        if(request.equals(Constants.changeCollaborationType)){
                        	GlobalVariables.setCollaborationType((String)dataArray[1]);
                        	logger.info("Collaboration type changed to " + dataArray[1]);
                            Platform.runLater(() -> diagramController.setServerLabel("User: "+ GlobalVariables.getUserName() +
                            		"\nCollaboration type: " + GlobalVariables.getCollaborationType() +
                            		"\nClient mode"));
                            Graph graph = (Graph) dataArray[2];
                            graph.addRemotePropertyChangeListener(ClientController.this);
                            Platform.runLater(() -> diagramController.load(graph, true));
                        }
                    	else if (object instanceof String[]){
                            Platform.runLater(() -> diagramController.remoteCommand((String[])object));
                    	}
                    }
            	}
            }
        });
    }

    public boolean connect(){
    	logger.debug("connect()");
        client.start();
        try {
            client.connect(5000, serverIp, port, port);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Could not connect to server");
            alert.setContentText("Unable to connect to: " + serverIp + ":" + port);
            alert.showAndWait();
            client.close();
            return false;
        }
        Object[] dataArray = {Constants.requestGraph, GlobalVariables.getUserName()};
        client.sendTCP(dataArray);
        return true;
    }

    public void close(){
    	logger.debug("close()");
        client.close();
    }

    /**
     * Handles property changes in the model and forwards them to server.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	logger.debug("propertyChange()");

    	String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.sketchAdd)){
        	String[] dataArray = { Constants.sketchAdd, GlobalVariables.getUserName()}; //Because serializing Sketch was tricky
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchPoint)){
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchPoint, sketch.getId(),
                    Double.toString(point.getX()), Double.toString(point.getY()), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchStart)) {
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchStart, sketch.getId(),
                    Double.toString(point.getX()), Double.toString(point.getY()), sketch.getColor().toString(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.sketchRemove)){
        	String[] dataArray = {Constants.sketchRemove, (String)evt.getNewValue(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if(propertyName.equals(Constants.NodeAdd)) {
            AbstractNode node = (AbstractNode) evt.getNewValue();
            Object[] dataArray = {node, GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.NodeRemove)){
        	String[] dataArray = {Constants.NodeRemove, (String)evt.getNewValue(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.EdgeAdd)){
            AbstractEdge edge = (AbstractEdge) evt.getNewValue();
            Object[] dataArray = {edge, GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if(propertyName.equals(Constants.EdgeRemove)) {
        	String [] dataArray = {Constants.EdgeRemove, (String)evt.getNewValue(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTranslateX) || propertyName.equals(Constants.changeNodeTranslateY)) { //NodeX/Y not needed
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(),
            		Double.toString(node.getTranslateX()),
            		Double.toString(node.getTranslateY()), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeWidth) || propertyName.equals(Constants.changeNodeHeight)){
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(),
            		Double.toString(node.getWidth()), 
            		Double.toString(node.getHeight()), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTitle)) {
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), node.getTitle(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeClassNodeAttribute)){
        	Object[] sourceDataArray = (Object[]) evt.getSource();
            ClassNode node = (ClassNode) sourceDataArray[0];
            String newValueStr = (String) sourceDataArray[1];
            Object[] dataArray = {propertyName, node.getId(), newValueStr, GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeClassNodeOperation)){
        	Object[] sourceDataArray = (Object[]) evt.getSource();
            ClassNode node = (ClassNode) sourceDataArray[0];
            String newValueStr = (String) sourceDataArray[1];
            Object[] dataArray = {propertyName, node.getId(), newValueStr, GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if(propertyName.equals(Constants.changeEdgeStartMultiplicity) || propertyName.equals(Constants.changeEdgeEndMultiplicity)){
            AbstractEdge edge = (AbstractEdge) evt.getSource();
            String[] dataArray = {propertyName, edge.getId(), edge.getStartMultiplicity(), edge.getEndMultiplicity(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
        else if(propertyName.equals(Constants.changeLabel)){
            AbstractEdge edge = (AbstractEdge) evt.getSource();
            String[] dataArray = {propertyName, edge.getId(), edge.getLabel(), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateX)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateX()), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateY)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateY()), GlobalVariables.getUserName()};
            client.sendTCP(dataArray);
        }
    }

    private void initKryo(Kryo kryo){
    	logger.debug("initKryo()");
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
        kryo.register(AbstractEdge.Direction.class);
        kryo.register(String[].class);
        kryo.register(Object[].class);
    }

    public void closeClient(){
    	logger.debug("closeClient()");
        client.close();
    }
}
