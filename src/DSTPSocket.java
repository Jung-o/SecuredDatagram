import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

public class DSTPSocket {
    private DatagramSocket socket;
    private Key encryptionKey;
    private Key macKey;
    private Cipher cipher;
    private Mac mac;
    private MessageDigest messageDigest;
    private boolean useHMAC;

    public DSTPSocket(int port, Key encryptionKey, Key macKey, Cipher cipher, Mac mac, MessageDigest messageDigest, boolean useHMAC) throws Exception {        this.socket = new DatagramSocket(port);
        this.encryptionKey = encryptionKey;
        this.macKey = macKey;
        this.cipher = cipher;
        this.mac = mac;
        this.messageDigest = messageDigest;
        this.useHMAC = useHMAC;
    }

    // Send encrypted packet
    public void send(byte[] data, InetAddress address, int port) throws Exception {
        DSTPPacket packet = new DSTPPacket(data, encryptionKey, macKey, cipher, mac, messageDigest, useHMAC);
        byte[] secureData = packet.toBytes();

        // Create a DatagramPacket and send
        DatagramPacket datagramPacket = new DatagramPacket(secureData, secureData.length, address, port);
        socket.send(datagramPacket);
    }

    // Receive and decrypt packet
    public byte[] receive() throws Exception {
        byte[] buffer = new byte[2048]; // Ensure buffer is large enough
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        byte[] receivedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        return DSTPPacket.decryptPacket(receivedData, encryptionKey, macKey, cipher, mac, messageDigest, useHMAC);
    }

    // Simulate an attack by modifying received data
    public byte[] simulateAttack() throws Exception {
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
        return DSTPPacket.decryptPacket(receivedData, encryptionKey, macKey, cipher, mac, messageDigest, useHMAC);
    }

    // Close the socket
    public void close() {
        socket.close();
    }
}
