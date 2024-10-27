/*
* hjStreamServer.java 
* Streaming server: emitter of video streams (movies)
* Can send in unicast or multicast IP for client listeners
* that can play in real time the transmitted movies
*/

import java.io.*;
import java.net.*;

class hjStreamServer {

	static public void main( String []args ) throws Exception {
	        if (args.length != 3)
	        {
	         System.out.println("Use: hjStramSrver <movie> <ip-multicast-address> <port>");
	         System.out.println("Ex: hjStreamSrver  <movie> 224.2.2.2 9000");		 		 
	         System.out.println(" or: hjStreamSrver  <movie> <ip-unicast-address> <port>");
	         System.out.println("Ex: hjStreamSrver  <movie> 127.0.0.1 10000");		 		 		 

	         System.exit(-1);
	         }

		DSTPConfig config = null;
		try {
			// Load configuration from file
			config= new DSTPConfig("configuration.txt");
		} catch (Exception e) {
			System.err.println("Couldn't file configuration.txt file...") ;
			System.exit(0) ;
		}

		int size;
		int count = 0;
 		long time;
		DataInputStream g = new DataInputStream( new FileInputStream(args[0]) );
		InetSocketAddress addr =
		    new InetSocketAddress(args[1],Integer.parseInt(args[2]));

		DSTPSocket dstpSocket = null;
		if(addr.getAddress().isMulticastAddress()){
			MulticastSocket ms = new MulticastSocket();
			dstpSocket = new DSTPSocket(ms, config);
		}
		else{
			DatagramSocket s = new DatagramSocket();
			dstpSocket = new DSTPSocket(s, config);
		}

		long t0 = System.nanoTime(); // tempo de referencia
		long q0 = 0;

		while ( g.available() > 0 ) {
			size = g.readShort();
			time = g.readLong();
			if ( count == 0 ) q0 = time; // tempo de referencia no stream
			count += 1;
			byte[] buff = new byte[size];
			g.readFully(buff,0,size);
			dstpSocket.send(buff, addr.getAddress(), addr.getPort());
			long t = System.nanoTime();
			Thread.sleep( Math.max(0, ((time-q0)-(t-t0))/1000000) );

			System.out.print( "." );
		}

		System.out.println("\nEND ! packets with frames sent: "+count);
	}

}
