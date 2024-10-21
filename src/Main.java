import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        try {
            // Set up cryptographic keys and ciphers
            Key encryptionKey = new SecretKeySpec("0123456789abcdef".getBytes(), "AES");
            Key macKey = new SecretKeySpec("fedcba9876543210".getBytes(), "HmacSHA256");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Mac mac = Mac.getInstance("HmacSHA256");

            // Create secure sockets
            DSTPSocket senderSocket = new DSTPSocket(8888, encryptionKey, macKey, cipher, mac);
            DSTPSocket receiverSocket = new DSTPSocket(8889, encryptionKey, macKey, cipher, mac);

            // Send secure data
            String message = "This is a secure message!";
            senderSocket.send(message.getBytes(), InetAddress.getByName("localhost"), 8889);

            // Receive and modify the packet (should fail the integrity check)
            try {
                byte[] receivedData = receiverSocket.simulateAttack();
                System.out.println("Received: " + new String(receivedData));
            } catch (SecurityException e) {
                System.out.println("Error: " + e);
            }

            System.out.println("\n\nSending a message longer than the block size.");
            String longMessage = """
                    Uma vez, quando tinha seis anos, vi uma gravura magnífica num livro sobre a floresta virgem que se\s
                    chamava Histо́rias Vividas. Era a imagem de uma jiboia a engolir uma fera. Aqui está a cо́pia do\s
                    desenho: No livro dizia: "As jiboias engolem a presa inteirinha, sem a mastigarem. Depois, não\s
                    conseguem mexer-se e ficam a dormir durante os seis meses da digestão." Então, pensei muito nas\s
                    aventuras da selva e, com um lápis de cor, consegui fazer o meu primeiro desenho. O meu desenho\s
                    número 1 era assim:""";
            senderSocket.send(longMessage.getBytes(), InetAddress.getByName("localhost"), 8889);

            byte[] receivedData = receiverSocket.receive();
            System.out.println("Received: " + new String(receivedData));

            senderSocket.close();
            receiverSocket.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
