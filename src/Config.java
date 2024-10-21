import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Map;

public class Config {
    private Key encryptionKey;
    private Key macKey;
    private Cipher cipher;
    private Mac mac;
    private MessageDigest messageDigest;
    private boolean useHMAC;

    public Config(String configFilePath) throws Exception {
        // Parse the configuration file
        Map<String, String> config = ConfigParser.parseConfig(configFilePath);

        // Extract configuration parameters
        String cipherAlgorithm = config.get("CONFIDENTIALITY");
        String symmetricKeyHex = config.get("SYMMETRIC_KEY");
        int symmetricKeySize = Integer.parseInt(config.get("SYMMETRIC_KEY_SIZE"));
        String ivHex = config.get("IV");
        String integrityType = config.get("INTEGRITY");
        String hashAlgorithm = config.get("H");
        String macAlgorithm = config.get("MAC");
        String macKeyHex = config.get("MACKEY");
        int macKeySize = Integer.parseInt(config.get("MACKEY_SIZE"));

        // Initialize encryption key
        if (symmetricKeyHex.length() * 8 != symmetricKeySize) {
            throw new IllegalArgumentException("Symmetric key size does not match the provided key.");
        }
        this.encryptionKey = new SecretKeySpec(symmetricKeyHex.getBytes(), extractAlgorithm(cipherAlgorithm));

        // Initialize cipher
        this.cipher = Cipher.getInstance(cipherAlgorithm);
        if (ivHex != null && !ivHex.equalsIgnoreCase("NULL")) {
            byte[] ivBytes = HexUtils.hexStringToByteArray(ivHex);
            this.cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(ivBytes));
        } else {
            this.cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        }

        if ("HMAC".equalsIgnoreCase(integrityType)) {
            this.useHMAC = true;
            // Initialize MAC key
            if (macKeyHex.length() * 8 != macKeySize) {
                throw new IllegalArgumentException("MAC key size does not match the provided key.");
            }
            this.macKey = new SecretKeySpec(macKeyHex.getBytes(), macAlgorithm);

            // Initialize MAC
            this.mac = Mac.getInstance(macAlgorithm);
            this.mac.init(macKey);
        } else if ("H".equalsIgnoreCase(integrityType)) {
            this.useHMAC = false;
            // Initialize MessageDigest for hashing
            this.messageDigest = MessageDigest.getInstance(hashAlgorithm);
        } else {
            throw new IllegalArgumentException("Invalid INTEGRITY type: must be either 'HMAC' or 'HASH'");
        }
    }

    public Key getEncryptionKey() {
        return encryptionKey;
    }

    public Key getMacKey() {
        return macKey;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public Mac getMac() {
        return mac;
    }

    public MessageDigest getMessageDigest() {
        return messageDigest;
    }

    public boolean isUseHMAC() {
        return useHMAC;
    }

    public static String extractAlgorithm(String algorithm) {
        return algorithm.split("/")[0];
    }
}
