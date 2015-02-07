import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Reciever1b {

	public static final int MAXIMUM_PACKET_SIZE = 1024;
	public static final String HOST = "localhost";

	private int port_number;
	private DatagramSocket recieving_sock;
	private DatagramSocket sending_sock;
	private InetAddress host;

	private boolean debug = true;
	

	public Reciever1b(String host_name, int port_number) {
		this.port_number = port_number;
		try {
			this.host = InetAddress.getByName(host_name);
		} catch (Exception e) {
			System.out.print(e);
		}

	}

	public void recieve() {
		System.out.print("Reciever Starting");
		try {
			
			// Open socket for recieving & listen on specified port
			recieving_sock = new DatagramSocket(port_number);
			
			// Open socket for sending
			sending_sock = new DatagramSocket();
			
			// New buffer for holding incoming packets
			byte[] buffer = new byte[MAXIMUM_PACKET_SIZE];
			
			// The file and the new output stream
			File file = new File("recieved_image_b.jpg");
			FileOutputStream file_output_stream = new FileOutputStream(file);
			
			// New datagram packets
			DatagramPacket incoming_packet = new DatagramPacket(buffer,	buffer.length);
			
			// keep track of bytes for 
			int num_bytes_recieved = 0;
			
			// initialize to -1 (not a possible packet number)
			// for no packets yet recieved
			int last_packet_number = -1;

			while (true) {
				// recieve the newest packet
				recieving_sock.receive(incoming_packet);
				byte[] data = incoming_packet.getData();

				// get the header from the most recent packet
				byte[] header = Arrays.copyOfRange(data, 0, 2);

				// identify the packet number
				short packet_number = ByteBuffer.wrap(header).getShort();

				// if this packet has already been recieved
				if (last_packet_number == packet_number){
					// just send an ack
					send_acknowledgement(packet_number);
				
				// if this is a new packet
				}else{
					
					// find the flag byte
					int last_packet = (int) data[2];

					// write the packet data to file.
					file_output_stream.write(data, 3, data.length - 3);

					// keep track of the bytes recieved for debug
					num_bytes_recieved += data.length - 3;

					// print for debug
					if (debug) {
						System.out.println(" number of bytes recieved: "
								+ num_bytes_recieved
								+ "\n recieved packet number: " + packet_number
								+ "\n of length " + (data.length - 3)
								+ "\n and the bit flag is " + last_packet
								+ "\n ********************************");
					}
					
					// send the client an acknowledgment of recipt
					send_acknowledgement(packet_number);
					Thread.sleep(20);

					// if this is the last packet, shut everything down
					if (last_packet == 1) {
						file_output_stream.close();
						System.exit(1);
					}
				}
				last_packet_number = packet_number;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void send_acknowledgement(short packet_number) throws Exception {
		// put in a packet the ack'ed packet's num
		byte[] data = new byte[2]; 
		data = ByteBuffer.allocate(2).putShort(packet_number).array();
		// send the ack to the sender
		DatagramPacket datagram_packet = new DatagramPacket(data, data.length, host, port_number+1);
		sending_sock.send(datagram_packet);
	}

	public static void main(String args[]) throws Exception {
		if(args.length == 2){ // valid arguments, specify host
			
			int port_number = Integer.parseInt(args[0]);
			String host = args[1];
			Reciever1b reciever1b = new Reciever1b(host, port_number);
			reciever1b.recieve();
		
		}else if (args.length == 1){ // valid arguments, default host
		
			int port_number = Integer.parseInt(args[0]);
			Reciever1b reciever1b = new Reciever1b(Reciever1b.HOST, port_number);
			reciever1b.recieve();
		
		}else{ // invalid arguments
			System.out.println(
					"Usage: \n" +
					" Reciever1b <port number> <host> \n" +
					" or for default localhost \n" +
					" Reciever1b <port number> ");
		}
	}
}