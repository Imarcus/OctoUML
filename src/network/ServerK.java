package network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.gson.Gson;
import model.AbstractNode;
import model.ClassNode;
import model.Graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Created by Marcus on 2016-07-21.
 */
public class ServerK implements PropertyChangeListener {

    private Graph graph;
    private Server server;

    public ServerK(Graph pGraph) {
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
        //kryo.register(Graph.class);

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof String) {
                    String request = (String)object;
                    System.out.println(request);

                    //Graph graph = new Graph();
                    connection.sendTCP("Server svar");
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("AddNode")) {
            ClassNode node = (ClassNode) evt.getNewValue();
            server.sendToAllTCP(node);
        }
    }
}
