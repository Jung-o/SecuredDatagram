import java.net.DatagramPacket;

public class TestServer {
    public static void main(String[] args) {
        String configFilename = "configuration.txt";
        DSTPConfig config;
        try {
            // Load configuration from file
            config = new DSTPConfig(configFilename);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            return;
        }

        // Create secure sockets using configuration
        DSTPSocket socket;
        try {
            socket = new DSTPSocket(8889, config);
        } catch (Exception ex) {
            System.out.println("Couldn't create sockets");
            System.out.println("Exception: " + ex);
            return;
        }

        while (true){
            byte[] buffer = new byte[65536]; // Ensure buffer is large enough
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            byte[] receivedData = socket.receive(packet);
            System.out.println("Received: " + new String(receivedData));
        }

        //socket.close();
    }
}
