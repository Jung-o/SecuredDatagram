import java.net.DatagramPacket;

public class TestServer {
    public static void main(String[] args) {
        String configFilename = "configuration.txt";
        DSTPConfig config = new DSTPConfig(configFilename);

        // Create secure sockets using configuration
        DSTPSocket socket;
        try {
            socket = new DSTPSocket(8889, config);
        } catch (Exception ex) {
            System.out.println("Couldn't create sockets");
            System.out.println("Exception: " + ex);
            return;
        }

	System.out.println("Waiting to receive messages...");
        while (true){
            byte[] buffer = new byte[65536]; // Ensure buffer is large enough
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            byte[] receivedData = socket.receive(packet);
            System.out.println("Received: " + new String(receivedData));
        }

        //socket.close();
    }
}
