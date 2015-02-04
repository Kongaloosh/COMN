import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import org.omg.CORBA.PUBLIC_MEMBER;

/*
 *  Part 1b for assignment COMN --- based on RDT3.0
 * 
 */
public class Sender1b {
	// server for 1b.

	public static final int MAXIMUM_PACKET_SIZE = 1024; // in bytes => 1KB
	private static final String HOST = "localhost";
	private static final String FILE = "test.jpg";

	private int port_number;
	private DatagramSocket sending_sock = null;
	private DatagramSocket recieving_sock = null;
	private boolean debug = true;

	// private FileInputStream file_reader;
	BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
	private InetAddress host;

	public Sender1b(String host, int port_number) {
		this.port_number = port_number;
		try {
			this.host = InetAddress.getByName(Sender1b.HOST);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void send() {
		try {
			sending_sock = new DatagramSocket();
			recieving_sock = new DatagramSocket(port_number+1);
			// read in the image to be sent
			FileInputStream file_input_stream = new FileInputStream(FILE);

			int packet_length = 0; // the length of the current packet
			short packet_number = 0; // we use a short, because it's two bytes
			boolean last_packet = false; // notes whether this is the last packet to transfer
			int num_bytes_sent = 0;
			int remaining_data = file_input_stream.available();
			
			// while there's still more packets to send
			while (!last_packet) {
				
				//check what the remaining 
				remaining_data = file_input_stream.available();
				
				// If the amount we have remaining is greater than the previous
				if (remaining_data > MAXIMUM_PACKET_SIZE - 3) {
					packet_length = MAXIMUM_PACKET_SIZE - 3;
				} else {
					// if less than max size, we make the packet smaller
					packet_length = remaining_data;
					last_packet = true;
				}
				
				// to hold the packet
				byte[] data = new byte[packet_length + 3];
				
				// read into the data leaving the first three packets for header
				file_input_stream.read(data, 3, packet_length);
				
				num_bytes_sent += packet_length;

				// Assigning values to the headers
				
				// add the packet number to header
				byte[] header = ByteBuffer.allocate(2).putShort(packet_number)
						.array();
				data[0] = header[0];
				data[1] = header[1];
				
				// add the end-packet flag
				data[2] = (byte) (last_packet ? 1 : 0); 

				// Create packet and send
				DatagramPacket datagram_packet = 
						new DatagramPacket(data, data.length, host, port_number);
				sending_sock.send(datagram_packet);
					
				if (debug)
					System.out.println("number of bytes sent: "
							+ num_bytes_sent + "\n number of packets sent: "
							+ packet_number + "\n packet length:  "
							+ packet_length
							+ "\n ********************************");
				
				// Enter the wait for ack state
				boolean is_acknowledged = false;
				while(!is_acknowledged){
					
					// check for ack
					is_acknowledged = check_for_acknowledgements(packet_number);
					if (is_acknowledged)
						break;
					
					// retransmit
					sending_sock.send(datagram_packet);
				}
				packet_number++;
				Thread.sleep(20);
			}

		} catch (Exception e) {
			System.out.print(e);
		}
	}

	public boolean check_for_acknowledgements(short packet_number) throws Exception {
		// short for acknowledgement.
		byte[] buffer = new byte[2]; 
		
		DatagramPacket incoming_packet = new DatagramPacket(buffer, buffer.length);
		
		recieving_sock.setSoTimeout(100);
		
		try {
			recieving_sock.receive(incoming_packet);
			byte[]data = incoming_packet.getData();
			int ack = ByteBuffer.wrap(data).getShort();
			if (ack == packet_number){
				return true;
			}
		}catch(SocketTimeoutException ste){
			System.out.println("Socket Timedout waiting for a response");
			return false;
		}
		return false;
	}

	public static void main(String args[]) {
		Sender1b sender1b = new Sender1b(Sender1b.HOST, 7777);
		sender1b.send();
	}
}