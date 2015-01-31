import java.io.*;
import java.net.*;

/**
 * 
 * @author alex
 *	requirements:
 *		* transfer from the sender to the reciever on localhost
 *		* UDP
 *		* 10Mbps bandwidth
 *		* 10Ms one-way propagation delay
 *		* 0% loss
 *		* 16 message sequence number
 *		* byte flag for end of message
 */
public class Sender1a {
	//server for 1a.
	
	public static final int PACKET_SIZE = 1024; //in bytes => 1KB
	private static final String HOST = "localhost";
	private int port_number;
	private DatagramSocket sock = null;
	//private FileInputStream file_reader;
	BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
	
	public Sender1a(String host, int port_number){
		this.port_number = port_number;
	}
	
	public void send (){
		try {
			sock = new DatagramSocket();
			InetAddress host = InetAddress.getByName(Sender1a.HOST);

			String message = cin.readLine();
			byte[] buffer = message.getBytes();
			
			DatagramPacket datagram_packet = new DatagramPacket(buffer, buffer.length, host, port_number);
			sock.send(datagram_packet);

		} catch (Exception e) {
			System.out.print(e);
		}			
	}
	
	public static void main(String args[]){
		Sender1a sender1a = new Sender1a(Sender1a.HOST, 7777);
		sender1a.send();
	}
}
