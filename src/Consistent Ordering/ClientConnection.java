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

import java.util.LinkedList;
import java.util.Queue;


public class ClientConnection implements Runnable { 
    public static const ERROR = "error"; 

    // Queues mssgs from all clients in 'author: mssg' format.
    private static List<String> mssgQueue; 

    public const String name;
    private Socket socket;

    private InputStream rawIn;
    private DataInputStream in;
    private OutputStream rawOut;
    private DataOutputStream out;


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
        while (!Thread.currentThread().isInterrupted()) {
            String mssg = this.readMessageAsString(); 

            if (mssg == ClientConnection.ERROR) {
                this.disconnect("Read Failure.");
                break;

            } else {
                mssgQueue.add(this.name + ": " + mssg);
            }
        }

        this.Thread.stop(); // Properly kill thread.
    }

    /**
     * Reads message from the client, and returns as a string.
     */
    public String readMessageAsString() {
        String mssg = "";
         
        try {
            // readUTF is blocking, so ensure socket has data to read before calling
            if (rawIn.available() > 0) {
                String mssg = in.readUTF();
            }
        
        } catch (Exception err) {
            return ClientConnection.ERROR;
        }

        return mssg;
    }

    public Scannr readMessage() {
        Scanner input;

        try {
            if (inputStream.available() > 0) {
                input = new Scanner(socket.getInputStream);
            }

        } catch (Exception err) {
            input = Scanner.error;
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
        currentThread.stop();
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

