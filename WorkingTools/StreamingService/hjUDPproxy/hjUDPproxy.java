/* hjUDPproxy, for use in 2024
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

class hjUDPproxy {
    public static void main(String[] args) throws Exception {
        if (args.length != 2)
        {
            System.out.println("Use: hjUDPproxy <endpoint1> <endpoint2>");
            System.out.println("<endpoint1>: endpoint for receiving stream");
            System.out.println("<endpoint2>: endpoint of media player");

            System.out.println("Ex: hjUDPproxy 224.2.2.2:9000  127.0.0.1:8888");
            System.out.println("Ex: hjUDPproxy 127.0.0.1:10000 127.0.0.1:8888");
            System.exit(0);
        }

        String remote=args[0]; // receive mediastream from this rmote endpoint
        String destinations=args[1]; //resend mediastream to this destination endpoint


        DSTPConfig config = new DSTPConfig("configuration.txt");

        InetSocketAddress inSocketAddress = parseSocketAddress(remote);
        Set<SocketAddress> outSocketAddressSet = Arrays.stream(destinations.split(",")).map(s -> parseSocketAddress(s)).collect(Collectors.toSet());

        DSTPSocket dstpSocket = null;

        if(inSocketAddress.getAddress().isMulticastAddress()){
            MulticastSocket ms = new MulticastSocket(inSocketAddress.getPort());
            ms.joinGroup(InetAddress.getByName(inSocketAddress.getHostName()));
            dstpSocket = new DSTPSocket(ms, config);
        }
        else{
            DatagramSocket inSocket = new DatagramSocket(inSocketAddress);
            dstpSocket = new DSTPSocket(inSocket, config);
        }
        int countframes=0;
        DatagramSocket outSocket = new DatagramSocket();

        byte[] buffer = new byte[4 * 1024];
        System.out.println("Sending frames...");
        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
            byte[] decryptedData = dstpSocket.receive(inPacket);

            for (SocketAddress outSocketAddress : outSocketAddressSet)
            {
                outSocket.send(new DatagramPacket(decryptedData, inPacket.getLength(), outSocketAddress));
            }
        }
    }

    private static InetSocketAddress parseSocketAddress(String socketAddress)
    {
        String[] split = socketAddress.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host, port);
    }
}
