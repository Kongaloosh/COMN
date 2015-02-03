import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

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
	private boolean debug = true;
	
	private InetAddress host;
	
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
			
			int packet_length = 0; // the length of the current packet
			short packet_number = 0; // we use a short, because it's two bytes
			boolean last_packet = false; // notes whether this is the last packet to transfer
			int num_bytes_sent = 0;
			
			while (file_input_stream.available() > 0) { //while there's still more to read
				int remaining_data = file_input_stream.available();
				
				//determine how long the packet should be
				if (remaining_data > MAXIMUM_PACKET_SIZE-3) {
					packet_length = MAXIMUM_PACKET_SIZE-3;
				}else{
					// if we don't have a more than 1024 bytes left, we can make the packet smaller
					packet_length = remaining_data;
					last_packet = true;
				}
				
				byte[] data = new byte[packet_length+3]; // we make the array which will hold the packet 
				file_input_stream.read(data, 3, packet_length); // we offset by three as we need a 3byte header
				num_bytes_sent += packet_length;
				
				
				// Assigning values to the headers
				byte[] header = ByteBuffer.allocate(2).putShort(packet_number).array();
				data[0] = header[0];
				data[1] = header[1];
				data[2] = (byte) (last_packet ? 1 : 0); // add the flag to identify if it's the last packet
				
				DatagramPacket datagram_packet = new DatagramPacket(data, data.length, host, port_number);
				sock.send(datagram_packet);
				
				if (debug)
					System.out.println(
							"number of bytes sent: " + num_bytes_sent +
							"\n number of packets sent: " + packet_number +
							"\n packet length:  " + packet_length+
							"\n ********************************");
				packet_number ++;
				Thread.sleep(20);
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