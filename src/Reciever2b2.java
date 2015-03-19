import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Matric Number: s1210313
 */
public class Reciever2b2 {
/**
 *  Selective Repeat for University of Edinburgh COMN course 2014/2015
 */
	public static final int MAXIMUM_PACKET_SIZE = 1024;
	public static final String HOST = "localhost";

	private int port_number;
	private String host_name;
	private String file_name;
	private int retry_timeout;

	private DatagramSocket recieving_sock;
	private DatagramSocket sending_sock;
	private InetAddress host;

	private boolean debug = true;
	private ArrayList<Packet> recieved_packets = new ArrayList<Packet>();

	public Reciever2b2(int port_number, String file_name) {
		this.port_number = port_number;
		this.file_name = file_name;
	}

	public void recieve() {
		System.out.print("Reciever Starting");
		
		try {
			recieving_sock = new DatagramSocket(port_number);
			sending_sock = new DatagramSocket();
			byte[] buffer = new byte[MAXIMUM_PACKET_SIZE];
			File file = new File(file_name);
			FileOutputStream file_output_stream = new FileOutputStream(file);
			DatagramPacket incoming_packet = new DatagramPacket(buffer,
					buffer.length);
			int num_bytes_recieved = 0;
			int last_packet_number = -1;
			int last_packet = -1;
			
			while (true) {
				// until we recieve the last packet and shut-down
				
				/**
				 * PACKET RECIEVING AND PROCESSING
				 */
				recieving_sock.receive(incoming_packet);
				byte[] data = incoming_packet.getData();

				try {
					host = incoming_packet.getAddress();
				} catch (Exception e) {
					System.out.print(e);
				}

				byte[] header = Arrays.copyOfRange(data, 0, 2);
				short packet_number = ByteBuffer.wrap(header).getShort();
				
				/**
				 * BUFFERING AND ACKNOWLEDGEMENT
				 */
				if (packet_number <= last_packet_number) { // if we've already seen this packet...
					//simply acknowledge the packet
					send_acknowledgement(packet_number);

				} else if(packet_number == last_packet_number+1){ // if this is the next packet in sequence...
					//write to the file and send an acknowledgement
					last_packet = (int) data[2];
					file_output_stream.write(data, 3, data.length - 3);
					num_bytes_recieved += data.length - 3;
					last_packet_number = packet_number;
					send_acknowledgement(packet_number);
				
				} else {// we've recieved a packet out of sequence; buffer it
					/**
					 * we sort the buffer of packets such that the
					 * first one is always the oldest
					 */
					recieved_packets.add(new Packet(packet_number, data));
					Collections.sort(recieved_packets, new Comparator<Packet>() {
						@Override
						public int compare(Packet o1, Packet o2) {
							return o1.number - o2.number;
						}
					});
					num_bytes_recieved += data.length - 3;
					send_acknowledgement(packet_number);
					
					/**
					 * pop the elements in the buffer which match 
					 * where we need continue writing
					 */
					while (recieved_packets.size() > 0 && recieved_packets.get(0).number == last_packet_number +1) {
						Packet packet = recieved_packets.get(0);
						recieved_packets.remove(0);
						last_packet_number = packet.number;
						file_output_stream.write(packet.data, 3, packet.data.length - 3);
					}
				}

				if (debug) {
					System.out.println(" number of bytes recieved: "
							+ num_bytes_recieved
							+ "\n recieved packet number: " + packet_number
							+ "\n of length " + (data.length - 3)
							+ "\n and the bit flag is " + last_packet
							+ "\n ********************************");
				}

				if (last_packet == 1) { // if this is the last packet, close the file and exit
					file_output_stream.close();
					System.exit(1);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Sends an packet containing the number of an acknowledged packet
	 */
	public void send_acknowledgement(short packet_number) throws Exception {
		byte[] data = new byte[2];
		data = ByteBuffer.allocate(2).putShort(packet_number).array();
		DatagramPacket datagram_packet = new DatagramPacket(data, data.length,
				host, port_number + 1);
		sending_sock.send(datagram_packet);
	}

	public static void main(String args[]) throws Exception {
		/**
		 * java ReceiverX <Port> <Filename>
		 */
		if (args.length == 2) { // valid arguments, specify host

			int port_number = Integer.parseInt(args[0]);
			String file_name = args[1];
			Reciever2b2 reciever1b = new Reciever2b2(port_number, file_name);
			reciever1b.recieve();

		} else { // invalid arguments
			System.out.println("Usage: \n"
					+ "java Receiver1b <Port> <Filename>");
		}
	}

	public class Packet {
		int number;
		byte[] data;

		public Packet(int number, byte[] data) {
			this.number = number;
			this.data = data;
		}
	}
}