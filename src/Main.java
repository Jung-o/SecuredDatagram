import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        String configFilename = "configuration.txt";
        Config config;
        try {
            // Load configuration from file
            config = new Config(configFilename);
        } catch (Exception ex) {
            System.err.println("Couldn't load config from: " + configFilename);
            System.err.println("Exception: " + ex);
            ex.printStackTrace();
            return;
        }

        // Create secure sockets using configuration
        DSTPSocket senderSocket;
        DSTPSocket receiverSocket;
        try {
            senderSocket = new DSTPSocket(8888, config);
            receiverSocket = new DSTPSocket(8889, config);
        } catch (Exception ex) {
            System.err.println("Couldn't create sockets");
            System.err.println("Exception: " + ex);
            return;
        }
        // Send secure data
        String message = "This is a secure message!";
        try {
            senderSocket.send(message.getBytes(), InetAddress.getByName("localhost"), 8889);
        } catch (Exception ex) {
            System.err.println("Couldn't send message");
            System.err.println("Exception: " + ex);
            senderSocket.close();
            receiverSocket.close();
            return;
        }

        // Receive and modify the packet (should fail the integrity check)
        try {
            byte[] receivedData = receiverSocket.simulateAttack();
            System.out.println("Received: " + new String(receivedData));
        } catch (Exception e) {
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

        try {
            senderSocket.send(longMessage.getBytes(), InetAddress.getByName("localhost"), 8889);
        } catch (Exception ex) {
            System.err.println("Couldn't send message");
            System.err.println("Exception: " + ex);
            senderSocket.close();
            receiverSocket.close();
            return;
        }

        try {
            byte[] receivedData = receiverSocket.receive();
            System.out.println("Received: " + new String(receivedData));
        } catch (Exception ex) {
            System.err.println("Error when receiving the message message");
            System.err.println("Exception: " + ex);
            senderSocket.close();
            receiverSocket.close();
            return;
        }


        senderSocket.close();
        receiverSocket.close();
    }
}
