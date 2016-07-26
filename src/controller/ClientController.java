package controller;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import model.*;
import util.Constants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created when attempting to connect to a server.
 */
public class ClientController implements PropertyChangeListener {

    private MainController mainController;
    private Client client;
    private String serverIp;
    private int port;


    public ClientController(MainController pMainController, String pServerIp, int pPort) {
        mainController = pMainController;
        serverIp = pServerIp;
        port = pPort;

        client = new Client();
        client.start();
        try {
            client.connect(5000, pServerIp, port, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initKryo(client.getKryo());

        client.sendTCP("RequestGraph");

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof Sketch) {
                    Platform.runLater(() -> mainController.addSketch((Sketch)object, false, true));
                }
                else if (object instanceof AbstractNode) {
                    Platform.runLater(() -> mainController.createNodeView((AbstractNode)object, true));
                }
                else if (object instanceof AbstractEdge) {
                    Platform.runLater(() -> mainController.addEdgeView((AbstractEdge)object, true));
                }
                else if (object instanceof Graph){
                    Graph graph = (Graph) object;
                    graph.addRemotePropertyChangeListener(ClientController.this);
                    Platform.runLater(() -> mainController.load(graph, true));
                }
                else if (object instanceof String[]){
                    Platform.runLater(() -> mainController.remoteCommand((String[])object));
                }
            }
        });
    }

    /**
     * Handles property changes in the model and forwards them to server.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.sketchAdd)){
            Sketch sketch = (Sketch) evt.getNewValue();
            client.sendTCP(sketch);
        }
        else if (propertyName.equals(Constants.changeSketchPoint)){
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchPoint, Integer.toString(sketch.getId()),
                    Double.toString(point.getX()), Double.toString(point.getY())};
            client.sendTCP(dataArray);
        }
        else if (propertyName.equals(Constants.changeSketchStart)) {
            Sketch sketch = (Sketch) evt.getSource();
            Point2D point = (Point2D) evt.getNewValue();
            String[] dataArray = {Constants.changeSketchStart, Integer.toString(sketch.getId()),
                    Double.toString(point.getX()), Double.toString(point.getY())};
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
    }

    public void closeClient(){
        client.close();
    }
}
