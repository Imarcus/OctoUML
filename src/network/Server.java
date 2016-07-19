package network;

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
import java.net.SocketTimeoutException;

/**
 * Created by Marcus on 2016-07-18.
 */
public class Server extends Thread implements PropertyChangeListener {

    private ServerSocket serverSocket;
    private Graph graph;
    private Socket socket = null;



    public Server(int port, Graph pGraph) throws IOException
    {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Can't setup server on this port number: " + port);
        }
        graph = pGraph;
        //serverSocket.setSoTimeout(10000);
    }

    public void run()
    {
        graph.addPropertyChangeListener(this);

        try {
            System.out.println("Waiting for client on port " +
                    serverSocket.getLocalPort() + "...");
            socket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Can't accept client connection. ");
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(socket != null){
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                Document doc = PersistenceManager.createXmi(graph);
                StringWriter sw = new StringWriter();
                try {
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    transformer.transform(new DOMSource(doc), new StreamResult(sw));
                } catch (TransformerConfigurationException e){
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
                String data = sw.toString();

                out.writeUTF(data);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
