package network;

import controller.MainController;
import javafx.application.Platform;
import model.Graph;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.persistence.PersistenceManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.*;
import java.io.*;

public class Client extends Thread {

    private String serverName;
    private int port;
    private MainController mainController;

    public Client(String pServerName, int pPort, MainController pMainController)
    {
        serverName = pServerName;
        port = pPort;
        mainController = pMainController;
    }

    public void run() {
        try {

            while (true) {
                Socket socket = new Socket(serverName, port);
                //System.out.println("Connecting to " + serverName + " on port " + port);

                DataInputStream dataInput = new DataInputStream(socket.getInputStream());
                String graph = dataInput.readUTF();
                //System.out.println(graph);
                try {
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(graph));

                    final Document doc = db.parse(is);
                    Platform.runLater(() -> { mainController.load(PersistenceManager.importXMI(doc));});

                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }


                sleep(100);
                socket.close();
            }
        }   catch(IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
