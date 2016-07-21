package network;

import com.google.gson.Gson;
import model.ClassNode;
import model.Graph;
import org.w3c.dom.Document;
import util.persistence.PersistenceManager;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Marcus on 2016-07-18.
 */
public class ServerJ extends Thread implements PropertyChangeListener {

    private ServerSocket serverSocket;
    private Graph graph;
    private Socket clientSocket = null;



    public ServerJ(int port, Graph pGraph) throws IOException
    {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Can't setup server on this port number: " + port);
        }
        graph = pGraph;
    }

    public void run()
    {
        graph.addPropertyChangeListener(this);

        try {
            System.out.println("Waiting for client on port " +
                    serverSocket.getLocalPort() + "...");
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Can't accept client connection. ");
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(clientSocket != null){
            String data = "";
            if(evt.getPropertyName().equals("AddNode")) {
                Gson gson = new Gson();
                ClassNode node = (ClassNode) evt.getNewValue();
                data = gson.toJson(node);
            }
            try(OutputStreamWriter out = new OutputStreamWriter(
                    clientSocket.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(data);
            } catch (IOException e) {
                try {
                    clientSocket.close();
                    clientSocket = null;
                } catch (IOException x) {
                    System.out.println("Couldn't close client socket");
                }
                e.printStackTrace();
            }
        }
    }
}
