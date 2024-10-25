import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DSTPSocket {
    private final DatagramSocket socket;
    private final DSTPConfig config;
    private int sequenceNumber;
    private final Set<Integer> receivedSequenceNumbers;

    public DSTPSocket(int port, DSTPConfig config) throws Exception {
        this.socket = new DatagramSocket(port);
        this.config = config;
        this.sequenceNumber = 0;
        this.receivedSequenceNumbers = new HashSet<>();
    }

    // Send encrypted packet
    public void send(byte[] data, InetAddress address, int port) throws Exception {
        DSTPPacket packet = new DSTPPacket(data, sequenceNumber++, config); // increments sequenceNumber after sending
        byte[] securePayload = packet.getPayload();

        // Create a DatagramPacket and send
        DatagramPacket datagramPacket = new DatagramPacket(securePayload, securePayload.length, address, port);
        socket.send(datagramPacket);
    }

    // Receive and decrypt packet, discarding invalid packets
    public byte[] receive() {
        while (true) {
            byte[] buffer = new byte[4096]; // Ensure buffer is large enough
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(packet);

                byte[] receivedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                byte[] decryptedDataWithSequence = DSTPPacket.decryptPacket(receivedData, config);

                // Extract sequence number
                int sequenceNumber = ByteBuffer.wrap(Arrays.copyOfRange(decryptedDataWithSequence, 0, 2)).getShort();
                // Check for duplicate sequence number
                if (receivedSequenceNumbers.contains(sequenceNumber)) {
                    throw new SecurityException("Duplicate sequence number received: " + sequenceNumber);
                }

                // Store the received sequence number
                receivedSequenceNumbers.add(sequenceNumber);

                return Arrays.copyOfRange(decryptedDataWithSequence, 2, decryptedDataWithSequence.length);
            } catch (Exception _) {
            }
        }
    }

    public void sendAndModify(byte[] data, InetAddress address, int port) throws Exception {
        System.out.println("Simulating modify attack by a middleman.");

        DSTPPacket packet = new DSTPPacket(data, sequenceNumber++, config); // increments sequenceNumber after sending
        byte[] securePayload = packet.getPayload();

        // Simulate modification by flipping some bits in the payload
        securePayload[18] = (byte) (securePayload[18] ^ 0xFF);
        securePayload[19] = (byte) (securePayload[19] ^ 0xFF);
        securePayload[20] = (byte) (securePayload[20] ^ 0xFF);
        securePayload[21] = (byte) (securePayload[21] ^ 0xFF);

        // Create a DatagramPacket and send
        DatagramPacket datagramPacket = new DatagramPacket(securePayload, securePayload.length, address, port);
        socket.send(datagramPacket);
    }


    // Close the socket
    public void close() {
        socket.close();
    }
}