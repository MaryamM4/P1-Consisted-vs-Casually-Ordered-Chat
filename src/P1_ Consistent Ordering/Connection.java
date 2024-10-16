import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection {
    public final static String ERROR = "CLIENTCONNECTIONERROR";
    public final String name = read_client_name();
    
    private Socket socket;
    private boolean is_closed_ = true;

    private OutputStream raw_out;
    private DataOutputStream out;
    private InputStream raw_in;
    private DataInputStream in;

    Connection(Socket client_sock) {
        this.socket = client_sock;

        try {
            this.raw_out = this.socket.getOutputStream();
            this.raw_in = this.socket.getInputStream();
            
            this.out = new DataOutputStream(this.raw_out);
            this.in = new DataInputStream(this.raw_in);

            this.is_closed_ = false;

        } catch (IOException io_ex) {
            io_ex.printStackTrace();

            System.out.println("Connection failed to init streams.");
            System.out.println("Aborting: Closing client connection...");

            this.close();
        }
    }

    private String read_client_name() {
        String name_ = ERROR;

        try {
            if (this.in.available() > 0) {
                name_ = this.in.readUTF();
            }

        } catch (IOException io_ex) {
            io_ex.printStackTrace();
        }

        if (name_.equals(ERROR)) {
            System.out.println("Connection failed to read Client name.");
            System.out.println("Aborting: Closing client connection...");
            this.close();
        }

        return name_;
    }

    public String read_from_client() {
        String mssg = "";

        try {
            if (this.in.available() > 0) {
                mssg = in.readUTF();
            }

        } catch (IOException io_ex) {
            io_ex.printStackTrace();
            mssg = ERROR;
        }

        return mssg;
    }

    public boolean write_to_client(String mssg) {
        try {
            this.out.writeUTF(mssg); 

             // Data might not be sent immediatley if buffer not full. Force send.
            this.out.flush();
            return true;
            
        } catch (IOException io_ex) {
            io_ex.printStackTrace();
        }
        return false;
    }

    public boolean close() {
        try {
            this.out.close();
            this.in.close();
            this.socket.close();

            this.is_closed_ = true;

            System.out.println("Closed client connection for '" + this.name + "'.");
            return true;

        } catch (IOException io_ex) {
            io_ex.printStackTrace();
            System.out.println("\nFailed to close client connection for '"+ this.name + "'.");
        }
        return false;
    }

    public boolean is_closed() {
        return this.is_closed_;
    }
}
