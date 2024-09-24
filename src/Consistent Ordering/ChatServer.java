/**
* ChatServer.java:<p>
* Establishes a server socket an creates a list of client connections.

* @references:
* - https://www.baeldung.com/java-socket-connection-read-timeout
* - https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html 
*
* @author Maryam M 
*/

import java.net.*; // for Socket
import java.io.*; // for IOException

import java.lang.Math; // For Max

public class ChatServer { 
    public const int CONNECTION_TIMEOUT_MS = 500; // Timeout for accept (>=250 ms mandatory). 
    public const int READ_TIMEOUT_MS = 0;        // Timeout for read. Set to 0 for none. 

    private ServerSocket serverSocket;
    private Socket socket;

    private static List<ClientConnection> clientList; 
    private static List<String> mssgQueue; 

    /**
     * Constuctor.
     */
    public ChatServer(int port) {
        serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();

        // Determine how long the serverSocket.accept() method will block.
        serverSocket.setSoTimeout(Math.max(250, ChatServer.CONNECTION_TIMEOUT_MS));

        // Determine how long the socketInputStream.read() method will block.
        if (ChatServer.READ_TIMEOUT_MS > 0) { socket.setSoTimeout(ChatServer.READ_TIMEOUT_MS) };
    }

    /**
     * 
     */
    public static void start() {
        try {
            // Start a a thread to handle the broadcasting loop.
            Thread updateChatThread = new Thread(ChatServer.updateChatLoop);
            updateChatThread.start(); 

            // Start a loop that accepts new clients and adds a ClientConnection to the broadcast list.
            while (!serverSocket.isClosed()) {
                acceptConnection(); 
            }
        
        } catch (Exception err) {


        } finally {
            updateChatThread.interrupt(); 
            close();
        }
    }

    /**
     * Accepts a client, create a client connection, and adds it to the client list.
     */
    private static void acceptConnection() {
        // Accept is blocking, so catch timeout exception
        try {
            // Accept a client connection and create the socket
            Socket clientSocket = serverSocket.accept();

            // If return value has a reference, we got a new connection. 
            if (clientSocket != null) {
                String clientName = clientSocket.getInetAddress().getHostName();
                /**
                // Read the client name with readUTF
                String clientName = "NoName";
                if (in.available() > 0) {
                    clientName = in.readUTF();
                }

                // Create the ClientConnection after, so it doesn't treat the name as a message.
                */

                // Create and start a ClientConnection thread to listen for client input. 
                ClientConnection clientConnection = new ClientConnection(clientName, clientSocket); 
                Thread connectionThread = new Thread(cc);
                connectionThread.start();

                // Add the thread to the broadcast list. 
                clientList.add(clientConnection);
            }

        } catch (SocketTimeoutException timedOut) {
            continue; // For "unblocking" the accept(). Not an error.
        }
    }

    private static void broadcastMssg(String mssg) {
        if (mssg != null && !(mssg.trim().isEmpty())) {
            for (ClientConnection client: clientList) {
                if (!client.writeMessage(mssg)) {
                    // If the Client has a write issue, disconnect and remove.
                    client.disconnect("Write Failure.");
                    clientList.remove(client);
                }
            }
        }
    }

    private static void updateChatLoop() {
        while (!serverSocket.isClosed()) {
            if (Thread.isInterrupted) { break; }

            broadcastMssg(ClientConnection.dequeueMssg());            
        }
    }

    /**
     * 
     */
    public static void close() {
        broadcastMssg("The chat Server is shutting down. Discontinuing chat update.");

        in.close();
        out.close();
        serverSocket.close();
    }

    /**
    * Usage: java ChatServer <server_port>
    *
    * @param args Receives a server port in args[0].
    */
    public static void main( String args[] ) {
        if (args.length != 1) { // Check # args.
            System.err.println( "Syntax: java ChatServer <server_port>" );
            System.exit(1);
        }

        // Convert args[0] into an integer that will be used as port.
        int port = Integer.parseInt(args[0]);

        // Instantiate the main body of ChatServer application.
        new ChatServer(port);
    }
}