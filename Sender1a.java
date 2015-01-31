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
	
	public static final int MAXIMUM_PACKET_SIZE = 1024; //in bytes => 1KB
	private static final String HOST = "localhost";
	private static final String FILE = "test.jpg";
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

			// read in the image to be sent
			FileInputStream file_input_stream = new FileInputStream(FILE);
			
			// the size of the file
			int size = file_input_stream.available();
			int num_packets = size/MAXIMUM_PACKET_SIZE;
			int packet_length = 0;
			
			while (file_input_stream.available() > 0) { //while there's still more to read
				int remaining_data = file_input_stream.available();
				
				//determine how long the packet should be
				if (remaining_data > MAXIMUM_PACKET_SIZE) {
					packet_length = MAXIMUM_PACKET_SIZE;
				}else{
					packet_length = remaining_data;
				}
				byte[] data = new byte[packet_length];
				file_input_stream.read(data);
				System.out.print(data);
				DatagramPacket datagram_packet = new DatagramPacket(data, data.length, host, port_number);
				sock.send(datagram_packet);

			}
			
		} catch (Exception e) {
			System.out.print(e);
		}			
	}
	
	public static void main(String args[]){
		Sender1a sender1a = new Sender1a(Sender1a.HOST, 7777);
		sender1a.send();
	}
}
