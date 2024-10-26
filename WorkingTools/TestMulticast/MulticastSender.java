import java.net.*;
import java.io.*;
import java.util.*;

public class MulticastSender {

    public static void main(String[] args ) throws Exception {
	    if( args.length != 3 ) {
		System.err.println("usage: java MulticastSender  grupo_multicast porto time-interval") ;
		System.exit(0) ;
	    }
 
    int more=20; // change if needed, send 20 time a MCAST message
    int port = Integer.parseInt( args[1]) ;
    InetAddress group = InetAddress.getByName( args[0] ) ;
    int timeinterval = Integer.parseInt( args[2]) ;
    String msg;

    if( !group.isMulticastAddress() ) {
	System.err.println("Multicast address required...") ;
	System.exit(0) ;
    }

    MulticastSocket ms = new MulticastSocket() ;
    DSTPConfig config = null;
    try {
        // Load configuration from file
        config = new DSTPConfig("configuration.txt");
    } catch (Exception e) {
        System.err.println("Couldn't file configuration.txt file...") ;
        System.exit(0) ;
    }

    DSTPSocket safeSendingSocket = new DSTPSocket(ms, config);

    do {
        String msgsecret="topcsecret message, sent on: ";
        String msgdate = new Date().toString();
        msg=msgsecret+msgdate;
        safeSendingSocket.send( msg.getBytes(), group, port ) ;

	--more;    // Tirar o comentario se quizer mandar apenas "more" numero de vezes

	try {
	    Thread.sleep(1000*timeinterval);
	} 
	catch (InterruptedException e) { }

    } while( more >0 ) ;
    msg="fim!";
        safeSendingSocket.send( msg.getBytes(), group, port ) ;
    ms.close();
	    
    }
}

