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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Forward changes in the graph to all clients.
 * Used by MainController for turning the diagram in to a host for clients to connect to.
 */
public class ServerController implements PropertyChangeListener {

    private Graph graph;
    private Server server;
    private AbstractDiagramController diagramController;
    private int port;
    private int nrClients = 0;

    public ServerController(Graph pGraph, AbstractDiagramController pDiagramController, int pPort) {
        diagramController = pDiagramController;
        port = pPort;
        graph = pGraph;
        graph.addRemotePropertyChangeListener(this);

        server = new Server();
        server.start();
        try {
            server.bind(port,port);
        } catch (IOException e){
            e.printStackTrace();
        }

        Platform.runLater(() -> diagramController.setServerLabel("Clients: " + Integer.toString(nrClients)));
        initKryo(server.getKryo());


        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof Sketch) {
                    Platform.runLater(() -> diagramController.addSketch((Sketch)object, false, true));
                }
                else if (object instanceof String) {
                    String request = (String)object;
                    if(request.equals(Constants.requestGraph)){
                        connection.sendTCP(diagramController.getGraphModel());
                    }
                }
                else if (object instanceof AbstractNode) {
                    server.sendToAllExceptTCP(connection.getID(), object);
                    Platform.runLater(() -> diagramController.createNodeView((AbstractNode)object, true));
                }
                else if (object instanceof AbstractEdge) {
                    server.sendToAllExceptTCP(connection.getID(), object);
                    Platform.runLater(() -> diagramController.addEdgeView((AbstractEdge)object, true));
                }
                else if (object instanceof String[]){
                    server.sendToAllExceptTCP(connection.getID(), object);
                    Platform.runLater(() -> diagramController.remoteCommand((String[])object));
                }
            }

            public void connected(Connection c){
                nrClients++;
                Platform.runLater(() -> diagramController.setServerLabel("Clients: " + Integer.toString(nrClients)));
            }

            public void disconnected(Connection c){
                nrClients--;
                Platform.runLater(() -> diagramController.setServerLabel("Clients: " + Integer.toString(nrClients)));
            }
        });

    }

    /**
     * Handles property changes in the model and forwards them to all connected clients.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.sketchAdd)){
            String[] dataArray = {Constants.sketchAdd};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchPoint)){
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchPoint, sketch.getId(),
                    Double.toString(point.getX()), Double.toString(point.getY())};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchStart)) {
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchStart, sketch.getId(),
                Double.toString(point.getX()), Double.toString(point.getY()), sketch.getColor().toString()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.sketchRemove)){
            String[] dataArray = {Constants.sketchRemove, (String)evt.getNewValue()};
            server.sendToAllTCP(dataArray);
        }
        else if(propertyName.equals(Constants.NodeAdd)) {
            AbstractNode node = (AbstractNode) evt.getNewValue();
            server.sendToAllTCP(node);
        }
        else if (propertyName.equals(Constants.NodeRemove)){
            String[] dataArray = {Constants.NodeRemove, (String)evt.getNewValue()};
            server.sendToAllTCP(dataArray);
        }
        else if (propertyName.equals(Constants.EdgeAdd)){
            AbstractEdge edge = (AbstractEdge) evt.getNewValue();
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
        } else if (propertyName.equals(Constants.changeSketchTranslateX)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateX())};
            server.sendToAllTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateY)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateY())};
            server.sendToAllTCP(dataArray);
        }
    }

    private void initKryo(Kryo kryo){
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
    }

    public void closeServer(){
        server.close();
        server.stop();
    }
}
