import java.net.*;
import java.io.*;
import java.util.*;

public class TFTPServer {

	public static void main(String argv[]) {
		try {
			//use port 6973
			DatagramSocket sock = new DatagramSocket(6973);

			DSTPConfig config = new DSTPConfig("configuration.txt");
			System.out.println("Server Ready.  Port:  " + sock.getLocalPort());

			// Listen for requests
			while (true) {
				DSTPSocket safeSocket = new DSTPSocket(sock, config);
				TFTPpacket in = TFTPpacket.receive(safeSocket);
				// receive read request
				if (in instanceof TFTPread) {
					System.out.println("Read Request from " + in.getAddress());
					TFTPserverRRQ r = new TFTPserverRRQ((TFTPread) in);
				}
				// receive write request
				else if (in instanceof TFTPwrite) {
					System.out.println("Write Request from " + in.getAddress());
					TFTPserverWRQ w = new TFTPserverWRQ((TFTPwrite) in);
				}
			}
		} catch (SocketException e) {
			System.out.println("Server terminated(SocketException) " + e.getMessage());
		} catch (TftpException e) {
			System.out.println("Server terminated(TftpException)" + e.getMessage());
		} catch (IOException e) {
			System.out.println("Server terminated(IOException)" + e.getMessage());
		} catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}