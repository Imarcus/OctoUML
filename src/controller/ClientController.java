package controller;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import model.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Marcus on 2016-07-21.
 */
public class ClientController {

    private MainController mainController;


    public ClientController(MainController pMainController) {
        mainController = pMainController;

        Client client = new Client();
        client.start();
        try {
            client.connect(5000, "127.0.0.1", 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Kryo kryo = client.getKryo();
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
        client.sendTCP("RequestGraph");

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof String) {
                } else if (object instanceof AbstractNode) {
                    Platform.runLater(() -> mainController.createNodeView((AbstractNode)object));
                } else if (object instanceof AbstractEdge) {
                    Platform.runLater(() -> mainController.addEdgeView((AbstractEdge)object));
                } else if (object instanceof Graph){
                    Graph graph = (Graph) object;
                    Platform.runLater(() -> mainController.load(graph));
                } else if (object instanceof String[]){
                    Platform.runLater(() -> mainController.remoteCommand((String[])object));
                }
            }
        });
    }
}
