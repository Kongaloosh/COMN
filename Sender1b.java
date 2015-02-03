import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * 
 * @author alex
 */
public class Sender1b {
	//server for 1b.
	
	public static final int MAXIMUM_PACKET_SIZE = 1024; //in bytes => 1KB
	private static final String HOST = "localhost"; 
	private static final String FILE = "test.jpg";
	
	private int port_number;
	private DatagramSocket sock = null;
	private boolean debug = true;
	
	//private FileInputStream file_reader;
	BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
	private InetAddress host;
	
	private int n;
	private int num_unaknowledged_packets = 0;
	
	public Sender1b(String host, int port_number, int n){
		this.port_number = port_number;
		this.n = n;
		try {
			this.host = InetAddress.getByName(Sender1b.HOST);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void send (){
		try {
			sock = new DatagramSocket();
		
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
				boolean is_aknowledged = false;
				while (!is_aknowledged){
					is_aknowledged = check_for_acknowledgements();
				}
				Thread.sleep(20);
			}
		} catch (Exception e) {
			System.out.print(e);
		}			
	}
	
	public boolean check_for_acknowledgements() throws Exception {
		System.out.println("acknowledgement");	
		byte[] buffer = new byte[2]; // short for acknowledgement.
		DatagramPacket incoming_packet = new DatagramPacket(buffer, buffer.length);
		System.out.println("here");
		sock.setSoTimeout(10);
		sock.receive(incoming_packet);
		System.out.println("here");
		byte[]data = incoming_packet.getData();
		if (ByteBuffer.wrap(data).getShort() == 1){
			System.out.println("Acknowledgement Recieved");
			return true;
		}
		return false;
	}
	
	public static void main(String args[]){
		Sender1b sender1b = new Sender1b(Sender1b.HOST, 7777,1);
		sender1b.send();
	}
}