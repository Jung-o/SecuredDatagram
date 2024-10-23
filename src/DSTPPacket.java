import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

public class DSTPPacket {
    private byte[] payload;
    private byte[] iv;
    private byte[] integrityCheck;

    // Constructor: Encrypt and create HMAC for the payload
    public DSTPPacket(byte[] data, DSTPConfig config) throws Exception {
        Key encryptionKey = config.getEncryptionKey();
        Key macKey = config.getMacKey();
        Cipher cipher = config.getCipher();
        Mac mac = config.getMac();
        MessageDigest messageDigest = config.getMessageDigest();
        boolean useHMAC = config.isUseHMAC();

        // Encrypt data
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        this.iv = cipher.getIV(); // Get IV if used
        this.payload = cipher.doFinal(data);

        // Generate HMAC or Hash for integrity
        if (useHMAC) {
            mac.init(macKey);
            this.integrityCheck = mac.doFinal(concatenate(iv, payload));
        } else {
            this.integrityCheck = messageDigest.digest(concatenate(iv, payload));
        }
    }

    // Combine IV, encrypted payload, and HMAC into a single byte array
    public byte[] toBytes() {
        return concatenate(iv, payload, integrityCheck);
    }

    // Parse a received packet to extract the encrypted payload, IV, and HMAC
    public static byte[] decryptPacket(byte[] receivedPacket, DSTPConfig config) throws Exception {
        Key encryptionKey = config.getEncryptionKey();
        Key macKey = config.getMacKey();
        Cipher cipher = config.getCipher();
        Mac mac = config.getMac();
        MessageDigest messageDigest = config.getMessageDigest();
        boolean useHMAC = config.isUseHMAC();

        int ivLength = config.getIvSize();
        int integrityLength = useHMAC ? mac.getMacLength() : messageDigest.getDigestLength();

        // Extract IV, payload, and HMAC
        byte[] iv = Arrays.copyOfRange(receivedPacket, 0, ivLength);
        byte[] encryptedPayload = Arrays.copyOfRange(receivedPacket, ivLength, receivedPacket.length - integrityLength);
        byte[] receivedIntegrityCheck = Arrays.copyOfRange(receivedPacket, receivedPacket.length - integrityLength, receivedPacket.length);

        // Verify integrity
        byte[] calculatedIntegrity;
        if (useHMAC) {
            mac.init(macKey);
            calculatedIntegrity = mac.doFinal(concatenate(iv, encryptedPayload));
        } else {
            calculatedIntegrity = messageDigest.digest(concatenate(iv, encryptedPayload));
        }
        if (!Arrays.equals(receivedIntegrityCheck, calculatedIntegrity)) {
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
