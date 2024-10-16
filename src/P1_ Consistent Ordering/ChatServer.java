import java.io.IOException;
import java.net.*;
import java.util.List;

public class ChatServer {
    public static final int CONNECTION_TIMEOUT_MS = 500; // Timeout for accept (>=250 ms mandatory). 
    public static final int READ_TIMEOUT_MS = 0;        // Timeout for read. Set to 0 for none. 

    public final int port;
    private ServerSocket server_socket;

     // Queues all client connections.
    private static List<Connection> clients; 

    public ChatServer(int server_port) {
        this.port = server_port;
    }

    // Closes client connection and removes it from the list
    private void close_and_remove_connection(Connection c_conn) {
        c_conn.close();
        clients.remove(c_conn);
    }

    // Send's mssg to all clients
    private void broadcast_mssg(String mssg) {
        for (Connection client_w: clients) {
            boolean succesful_send = client_w.write_to_client(mssg);

            // If sending is unsuccesful, close the connection and remove from list.
            if (!succesful_send) {
                close_and_remove_connection(client_w);
            }
        }
    }

    public void start() {
        try {
            // [0] Initialzie the server socket.
            this.server_socket = new ServerSocket(this.port);
            
            // Determine how long the serverSocket.accept() method will block.
            this.server_socket.setSoTimeout(Math.max(250, this.CONNECTION_TIMEOUT_MS));

            // Determine how long the socketInputStream.read() method will block.
            if (this.READ_TIMEOUT_MS > 0) { this.server_socket.setSoTimeout(this.READ_TIMEOUT_MS);}

            // Loop through the following
            while (true) {
                // [1] Accept a new client socket connection if there is any
                Socket c_socket = this.server_socket.accept();
                
                // [2] The connection class deal with reading the client's name using readUTF()
                Connection new_client_connection = new Connection(c_socket); 

                // [3] If succesful, dd the connection to the lsit of existing connections.
                if (!new_client_connection.is_closed()) {
                    clients.add(new_client_connection);
                }

                // [4] For each connection of the list:
                for (Connection client_r: clients) {
                        // Check the client status, if it's closed, don't bother.
                        if (client_r.is_closed()) {
                            close_and_remove_connection(client_r);
                            continue;
                        }

                        // (a) Receive a new message with readUTF( ) if there is any
                        String mssg = client_r.read_from_client();

                        // If there's an error reading, close the connection and remove it
                        if (mssg.equals(Connection.ERROR)) {
                            close_and_remove_connection(client_r);
                        }

                        if (!mssg.trim().isEmpty()) { // Only broadcast if a message is sent.
                            // (b) Add the client name in front of the message received.
                            mssg = client_r.name + ": " + mssg;

                            // (c) Write this message to all clients through all connections of the list. Use writeUTF( ).
                            broadcast_mssg(mssg);
                    
                        }
                } // Loop of reading from clients.
            }

        } catch (IOException io_err) {
            io_err.printStackTrace();
        }
    }

    public void close() {
        try {
            this.server_socket.close();
        
        } catch (IOException io_ex) {
            io_ex.printStackTrace();
            System.out.println("Error: Server failed to close socket.");
        }
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
        ChatServer server = new ChatServer(port);
    }
}

