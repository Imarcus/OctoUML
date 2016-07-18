package network;

import java.net.*;
import java.io.*;

public class Client extends Thread {

    private String serverName;
    private int port;

    public Client(String pServerName, int pPort)
    {
        serverName = pServerName;
        port = pPort;
    }

    public void run() {
        //while(true){
            try
            {
                System.out.println("Connecting to " + serverName +
                        " on port " + port);
                Socket client = new Socket(serverName, port);
                System.out.println("Just connected to "
                        + client.getRemoteSocketAddress());
                OutputStream outToServer = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.writeUTF("Hello from "
                        + client.getLocalSocketAddress());
                InputStream inFromServer = client.getInputStream();
                DataInputStream in =
                        new DataInputStream(inFromServer);
                System.out.println("Server says " + in.readUTF());
                client.close();
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        //}
    }
}
