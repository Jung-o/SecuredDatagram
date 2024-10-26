import java.net.InetAddress;

public class TestClient {
    public static void main(String[] args) {
        String configFilename = "configuration.txt";
        DSTPConfig config;
        try {
            // Load configuration from file
            config = new DSTPConfig(configFilename);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            return;
        }

        // Create secure sockets using configuration
        DSTPSocket socket;
        try {
            socket = new DSTPSocket(8888, config);
        } catch (Exception ex) {
            System.out.println("Couldn't create sockets");
            System.out.println("Exception: " + ex);
            return;
        }
        // Send secure data
        System.out.println("Sending a short message.");
        String message = "This is a secure message!";
        try {
            socket.send(message.getBytes(), InetAddress.getByName("localhost"), 8889);
        } catch (Exception ex) {
            System.out.println("Couldn't send message");
            System.out.println("Exception: " + ex);
            return;
        }

        System.out.println("Sending a long message.");
        String longMessage = """
                Uma vez, quando tinha seis anos, vi uma gravura magnífica num livro sobre a floresta virgem que se\s
                chamava Histо́rias Vividas. Era a imagem de uma jiboia a engolir uma fera. Aqui está a cо́pia do\s
                desenho: No livro dizia: "As jiboias engolem a presa inteirinha, sem a mastigarem. Depois, não\s
                conseguem mexer-se e ficam a dormir durante os seis meses da digestão." Então, pensei muito nas\s
                aventuras da selva e, com um lápis de cor, consegui fazer o meu primeiro desenho. O meu desenho\s
                número 1 era assim:""";

        try {
            socket.send(longMessage.getBytes(), InetAddress.getByName("localhost"), 8889);
        } catch (Exception ex) {
            System.out.println("Couldn't send message");
            System.out.println("Exception: " + ex);
            return;
        }
        socket.close();
    }
}
