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

	// the maximum packet size
	public static final int MAXIMUM_PACKET_SIZE = 1024;
	
	// the default host
	private static final String HOST = "localhost";
	
	// the file to be sent
	private static final String FILE = "test.jpg";

	// the arguments
	private int port_number;
	private String host_name;
	private String file_name;
	private int retry_timeout;

	// the specified host
	private InetAddress host;

	// the socket for sending
	private DatagramSocket sending_sock = null;
	
	// the socket for recieving
	private DatagramSocket recieving_sock = null;
	
	// for printing debug
	private boolean debug = true;

	private int retransmissions = 0;
	private int starttime = 0;
	// for reading the file to send in
	BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
	

	public Sender1b(String host_name, int port_number, String file_name, int retry_timeout ) {
		this.port_number = port_number;
		this.host_name = host_name;
		this.file_name = file_name;
		this.retry_timeout = retry_timeout;
		this.starttime = System.currentTimeMillis();
		try {
			this.host = InetAddress.getByName(host_name);
		} catch (Exception e) {
			System.out.print(e);
		}

	}

	public void send() {
		try {
			
			sending_sock = new DatagramSocket();
			recieving_sock = new DatagramSocket(port_number+1);
			
			// read in the image to be sent
			FileInputStream file_input_stream = new FileInputStream(file_name);

			// the length of the current packet
			int packet_length = 0; 
			
			// we use a short, because it's two bytes
			short packet_number = 0; 
			
			// notes whether this is the last packet to transfer
			boolean last_packet = false; 			
			
			// tracks number sent for debug
			int num_bytes_sent = 0 ;
			
			// what's rempaining to be sent
			int remaining_data = file_input_stream.available();
			
			// while there's still more packets to send
			while (!last_packet) {
				
				//check what the remaining 
				remaining_data = file_input_stream.available();
				
				// If the amount we have remaining is greater than packet size
				if (remaining_data > MAXIMUM_PACKET_SIZE - 3) {
					
					// the packet size is the maximum
					packet_length = MAXIMUM_PACKET_SIZE - 3;
				
				}else {
					
					// if less than max size, we make the packet smaller
					packet_length = remaining_data;
					last_packet = true;
				}
				
				// to hold the packet
				byte[] data = new byte[packet_length + 3];
				
				// read into the data leaving the first three packets for header
				file_input_stream.read(data, 3, packet_length);
				
				num_bytes_sent += packet_length;

				
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
					System.out.println(
							"number of bytes sent: " + num_bytes_sent
							+ "\n number of packets sent: " + packet_number
							+ "\n packet length:  " + packet_length +
							+ "\n number of retransmissions" + retransmissions
							+ "\n transmission rate " + ((System.currentTimeMillis()-starttime)/num_bytes_sent)/1000
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
		
		// set's the time the socket can be open before it times-out
		recieving_sock.setSoTimeout(retry_timeout);
		
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

	public static void main(String args[]) throws Exception {
		/**
		 * java SenderX localhost <Port> <Filename> [RetryTimeout]
		 */
		if(args.length == 4){ // valid arguments, specify host
			
			String host = args[0];			
			int port_number = Integer.parseInt(args[1]);
			String file_name = args[2];
			int retry_timeout = Integer.parseInt(args[3]);
			Sender1b sender1b = new Sender1b(host,port_number,file_name,retry_timeout);
			sender1b.send();
		
		}else{ // invalid arguments
			System.out.println(
					"Usage: \n" +
					" java Sender1b localhost <Port> <Filename> [RetryTimeout]");
		}
	}
}