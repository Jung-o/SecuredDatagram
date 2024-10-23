import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class DSTPConfig {
    private Key encryptionKey;
    private Key macKey;
    private Cipher cipher;
    private boolean useIV;
    private int ivSize;
    private Mac mac;
    private MessageDigest messageDigest;
    private boolean useHMAC;
    private String algorithm;

    public DSTPConfig(String configFilePath) throws Exception {
        // Parse the configuration file
        Map<String, String> config = parseConfig(configFilePath);

        // Extract configuration parameters
        String cipherAlgorithm = config.get("CONFIDENTIALITY");
        this.algorithm = extractAlgorithm(cipherAlgorithm);
        String symmetricKeyHex = config.get("SYMMETRIC_KEY");
        int symmetricKeySize = Integer.parseInt(config.get("SYMMETRIC_KEY_SIZE"));
        String ivHex = config.get("IV");
        int ivSize = Integer.parseInt(config.get("IV_SIZE"));
        String integrityType = config.get("INTEGRITY");
        String hashAlgorithm = config.get("H");
        String macAlgorithm = config.get("MAC");
        String macKeyHex = config.get("MACKEY");
        int macKeySize = Integer.parseInt(config.get("MACKEY_SIZE"));

        this.ivSize = ivSize;
        // Initialize encryption key
        if (symmetricKeyHex.length() * 8 != symmetricKeySize) {
            throw new IllegalArgumentException("Symmetric key size does not match the provided key.");
        }
        this.encryptionKey = new SecretKeySpec(symmetricKeyHex.getBytes(), algorithm);

        // Initialize cipher
        this.cipher = Cipher.getInstance(cipherAlgorithm);
        if (ivHex != null && !ivHex.equalsIgnoreCase("NULL")) {
            byte[] ivBytes = hexStringToByteArray(ivHex);
            this.useIV = true;
            this.cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(ivBytes));
        } else {
            this.useIV = false;
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

    public int getIvSize(){
        return ivSize;
    }

    public boolean doesUseHMAC() {
        return useHMAC;
    }

    public boolean doesUseIV(){
        return useIV;
    }

    public String getAlgorithm(){
        return algorithm;
    }

    public static String extractAlgorithm(String algorithm) {
        return algorithm.split("/")[0];
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static Map<String, String> parseConfig(String filePath) throws Exception {
        Map<String, String> config = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    config.put(key, value);
                }
            }
        }

        return config;
    }
}
