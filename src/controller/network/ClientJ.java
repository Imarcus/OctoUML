package controller.network;

import com.google.gson.Gson;
import controller.MainController;
import javafx.application.Platform;
import model.ClassNode;
import model.Graph;
import java.net.*;
import java.io.*;

public class ClientJ extends Thread {

    private String serverName;
    private int port;
    private MainController mainController;

    public ClientJ(String pServerName, int pPort, MainController pMainController)
    {
        serverName = pServerName;
        port = pPort;
        mainController = pMainController;
    }

    public void run() {
        try {
            Socket socket = new Socket(serverName, port);
            //System.out.println("Connecting to " + serverName + " on port " + port);
            while(true){
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input = br.readLine();
                if(input != null){
                    System.out.println(input);
                    Gson gson = new Gson();
                    ClassNode newNode = gson.fromJson(input, ClassNode.class);
                    Platform.runLater(() -> mainController.createNodeView(newNode));
                }
            }
            //socket.close();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
