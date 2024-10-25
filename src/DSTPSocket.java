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

    // Receive and decrypt packet
    public byte[] receive() throws Exception {
        byte[] buffer = new byte[4096]; // Ensure buffer is large enough
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
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
    }

    public byte[] receiveAndModify() throws Exception {
        System.out.println("Simulating receive and modify attack by a middleman.");
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        byte[] receivedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        byte[] surroundOriginalArray = Arrays.copyOfRange(receivedData, 18, 22);
        System.out.println("Original Received Data (cut to only show the changes) : " + Arrays.toString(surroundOriginalArray));

        // Simulate modification by flipping some bits in the payload
        receivedData[18] = (byte) (receivedData[8] ^ 0xFF);
        receivedData[19] = (byte) (receivedData[9] ^ 0xFF);
        receivedData[20] = (byte) (receivedData[10] ^ 0xFF);
        receivedData[21] = (byte) (receivedData[11] ^ 0xFF);

        byte[] surroundingModifiedArray = Arrays.copyOfRange(receivedData, 18, 22);
        System.out.println("Tampered Data (cut to only show the changes) : " + Arrays.toString(surroundingModifiedArray));

        // Try to decrypt the modified data (should fail integrity check)
        return DSTPPacket.decryptPacket(receivedData, config);
    }


    // Close the socket
    public void close() {
        socket.close();
    }
}