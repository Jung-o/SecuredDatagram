import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Arrays;

public class DSTPPacket {
    private byte[] payload;
    private byte[] iv;
    private byte[] hmac;

    // Constructor: Encrypt and create HMAC for the payload
    public DSTPPacket(byte[] data, Key encryptionKey, Key macKey, Cipher cipher, Mac mac) throws Exception {
        // Encrypt data
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        this.iv = cipher.getIV(); // Get IV if used
        this.payload = cipher.doFinal(data);

        // Generate HMAC for integrity
        mac.init(macKey);
        this.hmac = mac.doFinal(concatenate(iv, payload));
    }

    // Combine IV, encrypted payload, and HMAC into a single byte array
    public byte[] toBytes() {
        return concatenate(iv, payload, hmac);
    }

    // Parse a received packet to extract the encrypted payload, IV, and HMAC
    public static byte[] decryptPacket(byte[] receivedPacket, Key encryptionKey, Key macKey, Cipher cipher, Mac mac) throws Exception {
        int ivLength = 16; // Length of IV (based on encryption algorithm)
        int hmacLength = mac.getMacLength();

        // Extract IV, payload, and HMAC
        byte[] iv = Arrays.copyOfRange(receivedPacket, 0, ivLength);
        byte[] encryptedPayload = Arrays.copyOfRange(receivedPacket, ivLength, receivedPacket.length - hmacLength);
        byte[] receivedHmac = Arrays.copyOfRange(receivedPacket, receivedPacket.length - hmacLength, receivedPacket.length);

        // Verify HMAC integrity
        mac.init(macKey);
        byte[] calculatedHmac = mac.doFinal(concatenate(iv, encryptedPayload));
        if (!Arrays.equals(receivedHmac, calculatedHmac)) {
            throw new SecurityException("Packet integrity check failed");
        }

        // Decrypt the payload
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(iv));
        return cipher.doFinal(encryptedPayload);
    }

    private static byte[] concatenate(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }
}
