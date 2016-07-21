package network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import controller.MainController;
import javafx.application.Platform;
import model.AbstractNode;
import model.ClassNode;
import model.Graph;

import java.io.IOException;

/**
 * Created by Marcus on 2016-07-21.
 */
public class ClientK {

    private MainController mainController;


    public ClientK(MainController pMainController) {
        mainController = pMainController;

        Client client = new Client();
        client.start();
        try {
            client.connect(5000, "127.0.0.1", 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Kryo kryoClient = client.getKryo();
        kryoClient.register(ClassNode.class);
        kryoClient.register(AbstractNode.class);
        client.sendTCP("Hej");

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof String) {
                    System.out.println(object);
                } else if (object instanceof ClassNode) {
                    ClassNode node = (ClassNode) object;
                    System.out.println(node.toString());
                    Platform.runLater(() -> mainController.createNodeView(node));

                }
            }
        });
    }
}
