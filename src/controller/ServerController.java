package controller;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.gson.Gson;
import controller.MainController;
import edu.tamu.core.sketch.Stroke;
import javafx.application.Platform;
import javafx.geometry.Point2D;
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
    private int port;

    public ServerController(Graph pGraph, MainController pMainController, int pPort) {
        mainController = pMainController;
        port = pPort;
        graph = pGraph;
        graph.addRemotePropertyChangeListener(this);

        server = new Server();
        server.start();
        try {
            server.bind(port,54777);
        } catch (IOException e){
            e.printStackTrace();
        }

        initKryo(server.getKryo());


        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                System.out.println(object.toString());
                if (object instanceof Sketch) {
                    Platform.runLater(() -> mainController.addSketch((Sketch)object, false, true));
                }
                else if (object instanceof String) {
                    String request = (String)object;
                    if(request.equals("RequestGraph")){
                        connection.sendTCP(mainController.getGraphModel());
                    }
                }
                else if (object instanceof AbstractNode) {
                    Platform.runLater(() -> mainController.createNodeView((AbstractNode)object, true));
                }
                else if (object instanceof AbstractEdge) {
                    Platform.runLater(() -> mainController.addEdgeView((AbstractEdge)object, true));
                }
                else if (object instanceof Graph){
                    Graph graph = (Graph) object;
                    Platform.runLater(() -> mainController.load(graph, true));
                }
                else if (object instanceof String[]){
                    Platform.runLater(() -> mainController.remoteCommand((String[])object));
                }
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
                Double.toString(point.getX()), Double.toString(point.getY())};
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
        kryo.register(model.AbstractEdge.Direction.class);
        kryo.register(String[].class);
        kryo.register(Sketch.class);
        kryo.register(javafx.scene.shape.Path.class);
        kryo.register(com.sun.javafx.geom.RectBounds.class);
        kryo.register(com.sun.javafx.scene.CssFlags.class);
        kryo.register(javafx.scene.Node.class);
    }

    public void closeServer(){
        server.close();
    }
}
