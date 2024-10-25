import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class DSTPSocket {
    private final DatagramSocket socket;
    private final DSTPConfig config;

    public DSTPSocket(int port, DSTPConfig config) throws Exception {
        this.socket = new DatagramSocket(port);
        this.config = config;
    }

    // Send encrypted packet
    public void send(byte[] data, InetAddress address, int port) throws Exception {
        DSTPPacket packet = new DSTPPacket(data, config);
        byte[] secureData = packet.toBytes();

        // Create a DatagramPacket and send
        DatagramPacket datagramPacket = new DatagramPacket(secureData, secureData.length, address, port);
        socket.send(datagramPacket);
    }

    // Receive and decrypt packet
    public byte[] receive() throws Exception {
        byte[] buffer = new byte[4096]; // Ensure buffer is large enough
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        byte[] receivedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        return DSTPPacket.decryptPacket(receivedData, config);
    }

    // Simulate an attack by modifying received data
    public byte[] receiveAndModify() throws Exception {
        System.out.println("Simulating receive and modify attack by a middleman.");
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        byte[] receivedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        byte[] surroundOriginalArray = Arrays.copyOfRange(receivedData, 8, 12);
        System.out.println("Original Received Data (cut to only show the changes) : " + Arrays.toString(surroundOriginalArray));

        // Simulate modification by flipping some bits in the payload
        receivedData[8] = (byte) (receivedData[8] ^ 0xFF);
        receivedData[9] = (byte) (receivedData[9] ^ 0xFF);
        receivedData[10] = (byte) (receivedData[10] ^ 0xFF);
        receivedData[11] = (byte) (receivedData[11] ^ 0xFF);

        byte[] surroundingModifiedArray = Arrays.copyOfRange(receivedData, 8, 12);
        System.out.println("Tampered Data (cut to only show the changes) : " + Arrays.toString(surroundingModifiedArray));

        // Try to decrypt the modified data (should fail integrity check)
        return DSTPPacket.decryptPacket(receivedData, config);
    }

    // Close the socket
    public void close() {
        socket.close();
    }
}
