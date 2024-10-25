import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

public class DSTPPacket {
    private byte[] payload;

    public DSTPPacket(byte[] data, DSTPConfig config) throws Exception {
        Key encryptionKey = config.getEncryptionKey();
        Key macKey = config.getMacKey();
        Cipher cipher = Cipher.getInstance(config.getCipher());
        Mac mac = config.getMac();
        MessageDigest messageDigest = config.getMessageDigest();
        boolean useHMAC = config.doesUseHMAC();

        // Generate HMAC or Hash for integrity
        byte[] integrityCheck;
        if (useHMAC) {
            mac.init(macKey);
            integrityCheck = mac.doFinal(data);
        } else {
            integrityCheck = messageDigest.digest(data);
        }

        // Concatenate data with integrity check
        byte[] dataWithIntegrity = concatenate(data, integrityCheck);

        // Initialize cipher
        if (config.doesUseIV()) {
            byte[] ivBytes = config.getIv();
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(ivBytes));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        }

        // Encrypt concatenated data
        this.payload = cipher.doFinal(dataWithIntegrity);
    }

    // Combine IV and encrypted payload into a single byte array
    public byte[] getPayload() {
        return payload;
    }

    // Parse a received packet to extract the encrypted payload and IV
    public static byte[] decryptPacket(byte[] receivedPacket, DSTPConfig config) throws Exception {
        Key encryptionKey = config.getEncryptionKey();
        Cipher cipher = Cipher.getInstance(config.getCipher());
        Mac mac = config.getMac();
        MessageDigest messageDigest = config.getMessageDigest();
        boolean useHMAC = config.doesUseHMAC();

        // Extract IV and encrypted payload
        byte[] encryptedPayload = Arrays.copyOfRange(receivedPacket, 0, receivedPacket.length);

        // Decrypt the payload
        if (config.doesUseIV()) {
            byte[] ivBytes = config.getIv();
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(ivBytes));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        }
        byte[] decryptedDataWithIntegrity = cipher.doFinal(encryptedPayload);

        // Separate data and integrity check
        int integrityLength = useHMAC ? mac.getMacLength() : messageDigest.getDigestLength();
        byte[] data = Arrays.copyOfRange(decryptedDataWithIntegrity, 0, decryptedDataWithIntegrity.length - integrityLength);
        byte[] receivedIntegrityCheck = Arrays.copyOfRange(decryptedDataWithIntegrity, decryptedDataWithIntegrity.length - integrityLength, decryptedDataWithIntegrity.length);

        // Verify integrity
        byte[] calculatedIntegrity;
        if (useHMAC) {
            mac.init(config.getMacKey());
            calculatedIntegrity = mac.doFinal(data);
        } else {
            calculatedIntegrity = messageDigest.digest(data);
        }
        if (!Arrays.equals(receivedIntegrityCheck, calculatedIntegrity)) {
            throw new SecurityException("Packet integrity check failed");
        }

        return data;
    }

    private static byte[] concatenate(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, currentIndex, array.length);
                currentIndex += array.length;
            }
        }
        return result;
    }
}