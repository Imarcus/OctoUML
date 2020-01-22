package controller;

import com.esotericsoftware.kryo.Kryo;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.application.Platform;
import javafx.geometry.Point2D;
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
 * Forward changes in the graph to all clients.
 * Used by MainController for turning the diagram in to a host for clients to connect to.
 */
public class ServerController implements PropertyChangeListener {

	private static Logger logger = LoggerFactory.getLogger(ServerController.class);
    private Graph graph;
    private Server server;
    private AbstractDiagramController diagramController;
    private int port;
    private int nrClients = 0;

    public ServerController(Graph pGraph, AbstractDiagramController pDiagramController, int pPort) {
    	logger.debug("ServerController()");
        diagramController = pDiagramController;
        port = pPort;
        graph = pGraph;
        graph.addRemotePropertyChangeListener(this);

        // Increase of buffers size to avoid overflow. Defaults: 16384, 2048
        server = new Server(16384,16384);
        server.start();
        try {
            server.bind(port,port);
        } catch (IOException e){
            e.printStackTrace();
        }

        Platform.runLater(() -> diagramController.setServerLabel(
        		GlobalVariables.getString("user") + ": " + diagramController.getUserName() +
        		"\n" + GlobalVariables.getString("collaborationType") + ": " + GlobalVariables.getString(diagramController.getCollaborationType()) +
        		"\n" + GlobalVariables.getString("serverMode") +
        		" (" + GlobalVariables.getString("port") + ": " + port + ", " +
        		GlobalVariables.getString("clients") + ": " + Integer.toString(nrClients) + ")") );
        initKryo(server.getKryo());

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
            	if ( object instanceof Object[]) {
                	logger.debug("received()");
                	Object[] dataArray = (Object[]) object; 
                    if (dataArray[0] instanceof Sketch) {
                        Platform.runLater(() -> diagramController.addSketch((Sketch)dataArray[0], false, true));
                    }
                    else if (dataArray[0] instanceof String) {
                        String request = (String) dataArray[0];
                        if(request.equals(Constants.requestGraph)){
                        	Object[] sendDataArray = {diagramController.getGraphModel(),
                        			diagramController.getCollaborationType(),
                        			diagramController.getUserName()}; 
                            connection.sendTCP(sendDataArray);
                        }
                        else if (object instanceof String[]) {
                            server.sendToAllExceptTCP(connection.getID(), object);
                            Platform.runLater(() -> diagramController.remoteCommand((String[])object));
                        }
                    }
                    else if (dataArray[0] instanceof AbstractNode) {
                        server.sendToAllExceptTCP(connection.getID(), dataArray);
                        Platform.runLater(() -> diagramController.createNodeView((AbstractNode)dataArray[0], true));
                    }
                    else if (dataArray[0] instanceof AbstractEdge) {
                        server.sendToAllExceptTCP(connection.getID(), dataArray);
                        Platform.runLater(() -> diagramController.addEdgeView((AbstractEdge)dataArray[0], true));
                    }
            	} 
            }

            public void connected(Connection c){
                nrClients++;
                Platform.runLater(() -> diagramController.setServerLabel(
                		GlobalVariables.getString("user") + ": " + diagramController.getUserName() +
                		"\n" + GlobalVariables.getString("collaborationType") + ": " + GlobalVariables.getString(diagramController.getCollaborationType()) +
                		"\n" + GlobalVariables.getString("serverMode") +
                		" (" + GlobalVariables.getString("port") + ": " + port + ", " +
                		GlobalVariables.getString("clients") + ": " + Integer.toString(nrClients) + ")") );
            }

            public void disconnected(Connection c){
                nrClients--;
                Platform.runLater(() -> diagramController.setServerLabel(
                		GlobalVariables.getString("user") + ": " + diagramController.getUserName() +
                		"\n" + GlobalVariables.getString("collaborationType") + ": " + GlobalVariables.getString(diagramController.getCollaborationType()) +
                		"\n" + GlobalVariables.getString("serverMode") +
                		" (" + GlobalVariables.getString("port") + ": " + port + ", " +
                		GlobalVariables.getString("clients") + ": " + Integer.toString(nrClients) + ")") );
            }
        });

    }

    /**
     * Handles property changes in the model and forwards them to all connected clients.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	logger.debug("propertyChange()");
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.sketchAdd)){
            String[] dataArray = {Constants.sketchAdd, diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchPoint)){
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchPoint, sketch.getId(),
                    Double.toString(point.getX()), Double.toString(point.getY()), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchStart)) {
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchStart, sketch.getId(),
                Double.toString(point.getX()), Double.toString(point.getY()), sketch.getColor().toString(), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.sketchRemove)){
        	String[] dataArray = {Constants.sketchRemove, (String)evt.getNewValue(), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if(propertyName.equals(Constants.NodeAdd)) {
            AbstractNode node = (AbstractNode) evt.getNewValue();
            Object[] dataArray = {node, diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.NodeRemove)){
        	String[] dataArray = {Constants.NodeRemove, (String)evt.getNewValue(), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.EdgeAdd)){
            AbstractEdge edge = (AbstractEdge) evt.getNewValue();
            Object[] dataArray = {edge, diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if(propertyName.equals(Constants.EdgeRemove)) {
        	String [] dataArray = {Constants.EdgeRemove, (String)evt.getNewValue(), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTranslateX) || propertyName.equals(Constants.changeNodeTranslateY)) { //NodeX/Y not needed
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), Double.toString(node.getTranslateX()),
            		Double.toString(node.getTranslateY()), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeWidth) || propertyName.equals(Constants.changeNodeHeight)){
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), Double.toString(node.getWidth()),
            		Double.toString(node.getHeight()), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTitle)) {
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), node.getTitle(), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeClassNodeAttribute)){
        	AbstractNode node = (AbstractNode) evt.getSource();
            String newValueStr = (String) evt.getNewValue();
            String[] dataArray = {propertyName, node.getId(), newValueStr, diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeClassNodeOperation)){
        	AbstractNode node = (AbstractNode) evt.getSource();
            String newValueStr = (String) evt.getNewValue();
            String[] dataArray = {propertyName, node.getId(), newValueStr, diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if(propertyName.equals(Constants.changeEdgeStartMultiplicity) || propertyName.equals(Constants.changeEdgeEndMultiplicity)){
            AbstractEdge edge = (AbstractEdge) evt.getSource();
            String[] dataArray = {propertyName, edge.getId(), edge.getStartMultiplicity(),
            		edge.getEndMultiplicity(), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        }
        else if(propertyName.equals(Constants.changeLabel)){
                AbstractEdge edge = (AbstractEdge) evt.getSource();
                String[] dataArray = {propertyName, edge.getId(), edge.getLabel(), diagramController.getUserName()};
                server.sendToAllTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateX)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateX()), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateY)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateY()), diagramController.getUserName()};
            server.sendToAllTCP(dataArray);
        } else if (propertyName.equals(Constants.changeCollaborationType)) {
        	Object[] dataArray = {diagramController.getGraphModel(),
        			diagramController.getCollaborationType(),
        			diagramController.getUserName()}; 
            server.sendToAllTCP(dataArray);
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

    public void closeServer(){
    	logger.debug("closeServer()");
        server.close();
        server.stop();
    }
}
