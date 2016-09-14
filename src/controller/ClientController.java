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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Communicates changes in the graph to and from the host.
 * Used by MainController when connecting to a host.
 */
public class ClientController implements PropertyChangeListener {

    private AbstractDiagramController diagramController;
    private Client client;
    private String serverIp;
    private int port;


    public ClientController(AbstractDiagramController pDiagramController, String pServerIp, int pPort) {
        diagramController = pDiagramController;
        serverIp = pServerIp;
        port = pPort;

        client = new Client();

        initKryo(client.getKryo());

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof AbstractNode) {
                    Platform.runLater(() -> diagramController.createNodeView((AbstractNode)object, true));
                }
                else if (object instanceof AbstractEdge) {
                    Platform.runLater(() -> diagramController.addEdgeView((AbstractEdge)object, true));
                }
                else if (object instanceof Graph){
                    Graph graph = (Graph) object;
                    graph.addRemotePropertyChangeListener(ClientController.this);
                    Platform.runLater(() -> diagramController.load(graph, true));
                }
                else if (object instanceof String[]){
                    Platform.runLater(() -> diagramController.remoteCommand((String[])object));
                }
            }
        });
    }

    public boolean connect(){
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

        client.sendTCP(Constants.requestGraph);
        return true;
    }

    public void close(){
        client.close();
    }

    /**
     * Handles property changes in the model and forwards them to server.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.sketchAdd)){
            String[] dataArray = {Constants.sketchAdd}; //Because serializing Sketch was tricky
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchPoint)){
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchPoint, sketch.getId(),
                    Double.toString(point.getX()), Double.toString(point.getY())};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchStart)) {
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchStart, sketch.getId(),
                    Double.toString(point.getX()), Double.toString(point.getY()), sketch.getColor().toString()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.sketchRemove)){
            String[] dataArray = {Constants.sketchRemove, (String)evt.getNewValue()};
            client.sendTCP(dataArray);
        }
        else if(propertyName.equals(Constants.NodeAdd)) {
            AbstractNode node = (AbstractNode) evt.getNewValue();
            client.sendTCP(node);
        }
        else if (propertyName.equals(Constants.NodeRemove)){
            String[] dataArray = {Constants.NodeRemove, (String)evt.getNewValue()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.EdgeAdd)){
            AbstractEdge edge = (AbstractEdge) evt.getNewValue();
            client.sendTCP(edge);
        }
        else if(propertyName.equals(Constants.EdgeRemove)) {
            String [] dataArray = {Constants.EdgeRemove, (String)evt.getNewValue()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTranslateX) || propertyName.equals(Constants.changeNodeTranslateY)) { //NodeX/Y not needed
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), Double.toString(node.getTranslateX()), Double.toString(node.getTranslateY())};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeWidth) || propertyName.equals(Constants.changeNodeHeight)){
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), Double.toString(node.getWidth()), Double.toString(node.getHeight())};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeNodeTitle)) {
            AbstractNode node = (AbstractNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), node.getTitle()};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeClassNodeAttributes) || propertyName.equals(Constants.changeClassNodeOperations)){
            ClassNode node = (ClassNode) evt.getSource();
            String[] dataArray = {propertyName, node.getId(), node.getAttributes(), node.getOperations()};
            client.sendTCP(dataArray);
        }
        else if(propertyName.equals(Constants.changeEdgeStartMultiplicity) || propertyName.equals(Constants.changeEdgeEndMultiplicity)){
            AbstractEdge edge = (AbstractEdge) evt.getSource();
            String[] dataArray = {propertyName, edge.getId(), edge.getStartMultiplicity(), edge.getEndMultiplicity()};
            client.sendTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateX)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateX())};
            client.sendTCP(dataArray);
        } else if (propertyName.equals(Constants.changeSketchTranslateY)) {
            Sketch sketch = (Sketch) evt.getSource();
            String[] dataArray = {propertyName, sketch.getId(), Double.toString(sketch.getTranslateY())};
            client.sendTCP(dataArray);
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

    public void closeClient(){
        client.close();
    }
}
