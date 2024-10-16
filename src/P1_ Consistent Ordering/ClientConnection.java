/**
* ClientConnection.java:<p>
* Mantains a client socket connection and provides read/write functionality. 
*
* @author Maryam M (CSSE student, University of Washington, Bothell)
* @since (start date)
* @version (last date)
*/

import java.net.*; // for Socket
import java.io.*; // for IOException

import java.util.List;
import java.util.Scanner;



public class ClientConnection implements Runnable { 
    public static final String ERROR = "error"; 

    // Queues mssgs from all clients in 'author: mssg' format.
    private static List<String> mssgQueue; 

    public final String name;
    private final Socket socket;

    private OutputStream rawOut;
    private DataOutputStream out;
    private InputStream rawIn;
    private DataInputStream in;


    public ClientConnection(String name, Socket socket, DataInputStream in, DataOutputStream out) {       
        this.name = name;
        this.socket = socket;

        this.in = in;
        this.out = out;
    }

    /**
    * This method will be invoked when the object's thread is started. 
    */
    @Override
    public void run() {
        String reasonForDisconnect = ""; 

        try {
            while (!Thread.currentThread().isInterrupted()) {
                String mssg = this.readMessageAsString(); 

                if (mssg == null ? ClientConnection.ERROR == null : mssg.equals(ClientConnection.ERROR)) {
                    reasonForDisconnect = "Read Failure.";
                    break;

                } else {
                    mssgQueue.add(this.name + ": " + mssg);
                }
            }

        } catch (Exception err) {
            reasonForDisconnect = err.getMessage();

        } finally {
            this.disconnect(reasonForDisconnect);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Reads message from the client, and returns as a string.
     */
    public String readMessageAsString() {
        String mssg = "";
         
        try {
            // readUTF is blocking, so ensure socket has data to read before calling
            if (rawIn.available() > 0) {
                mssg = in.readUTF();
            }
        
        } catch (IOException err) {
            mssg = ClientConnection.ERROR;
        }

        return mssg;
    }

    public Scanner readMessage() {
        Scanner input = null;

        try {
            if (rawIn.available() > 0) {
                input = new Scanner(socket.getInputStream());
            }

        } catch (IOException err) {
            System.err.println("ClientConnection Error reading message: " + err.getMessage());
        }

        return input;
    }

    /**
     * Writes the message to the client.
     */
    public boolean writeMessage(String mssg) {
        try {
            out.writeUTF(mssg);
            return true;

        } catch (Exception err) {
            // log exception
        }

        return false;
    }

    public void disconnect(String reasonForDisconnect) {
        // Notify chat that user disconnected. 
        String mssg = "[" + this.name + " disconnected].";
        if (reasonForDisconnect != null && !reasonForDisconnect.trim().isEmpty()) {
            mssg = "[" + this.name + " disconnected: " + reasonForDisconnect + "]";
        }
        mssgQueue.add(mssg);

        // Close the socket, and stop the thread. 
        socket.close();
        Thread.currentThread().interrupt();
    }

    public void disconnect() {
        this.disconnect(null);
    }

    public static String dequeueMssg() {
        if (!mssgQueue.isEmpty()) {
            return mssgQueue.remove(0);
        }

        return ""; 
    }

}

