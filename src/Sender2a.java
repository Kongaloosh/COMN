import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.omg.CORBA.PUBLIC_MEMBER;

/*
 *  Part 1b for assignment COMN --- based on RDT3.0
 * 
 *  * implement a timer
 *  	- keep track of the time of the last unacked packet
 *  	- if the current time is after that period, retransmit everything.
 *  	- is it everything, or only that one?
 */
public class Sender2a {
	// server for 1b.
	public static final int MAXIMUM_PACKET_SIZE = 1024;
	private static final String HOST = "localhost";

	private static final String FILE = "test.jpg";

	private int port_number;
	private String host_name;
	private String file_name;
	private int retry_timeout;

	private InetAddress host;
	private DatagramSocket sending_sock = null;
	private DatagramSocket recieving_sock = null;
	private boolean debug = true;

	private int retransmissions = 0;
	private int starttime = 0;
	BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

	private ArrayList<Packet> acknowledged_packets = new ArrayList<Packet>();

	private int window_size;
	private int acked_packet_num;

	boolean last_packet = false;
	boolean stop = false;
	
	public Sender2a(String host_name, int port_number, String file_name,
			int retry_timeout, int window_size) throws Exception {

		this.port_number = port_number;
		this.host_name = host_name;
		this.file_name = file_name;
		this.retry_timeout = retry_timeout;
		this.starttime = (int) System.currentTimeMillis();
		this.host = InetAddress.getByName(host_name);
		this.window_size = window_size;
	}

	public void send() throws Exception {
		sending_sock = new DatagramSocket();

		FileInputStream file_input_stream = new FileInputStream(file_name);
		int packet_length = 0;
		short packet_number = 0;
		int num_bytes_sent = 0;
		int remaining_data = file_input_stream.available();

		Acknowledge acknowledge = new Acknowledge(port_number);
		acknowledge.start();

		while (!stop) {
			/**
			 * if we've acked a packet, add remove it and any packets which are before it
			 */
			while (acknowledged_packets.size() > 0
					&& acknowledged_packets.get(0).number < acked_packet_num) {
				acknowledged_packets.remove(0);
			}
	
			/**
			 * While we still have room in our window, send packets
			 */
			while (window_size > acknowledged_packets.size()) {
				remaining_data = file_input_stream.available();
				if (remaining_data > MAXIMUM_PACKET_SIZE - 3) {
					packet_length = MAXIMUM_PACKET_SIZE - 3;
				} else {
					packet_length = remaining_data;
					last_packet = true;
				}
				byte[] data = new byte[packet_length + 3];
				file_input_stream.read(data, 3, packet_length);
				num_bytes_sent += packet_length;
				byte[] header = ByteBuffer.allocate(2).putShort(packet_number)
						.array();
				data[0] = header[0];
				data[1] = header[1];
				data[2] = (byte) (last_packet ? 1 : 0);

				DatagramPacket datagram_packet = new DatagramPacket(data,
						data.length, host, port_number);
				sending_sock.send(datagram_packet);
				acknowledged_packets.add(new Packet(packet_number,
						datagram_packet, System.currentTimeMillis()));

				if (debug)
					System.out
							.println("number of bytes sent: " + num_bytes_sent
									+ "\n number of packets sent: " + packet_number
									+ "\n packet length:  " + packet_length
									+ "\n number of retransmissions: " + retransmissions
									+ "\n time: " + starttime
									+ "\n time delta: " + (System.currentTimeMillis() - starttime) 
									+ "\n transmission rate: " + ((float) (System.currentTimeMillis() - starttime) / 1000)/num_bytes_sent
									+ "\n remaining data: " + remaining_data
									+ "\n last packet: " + acked_packet_num
									+ "\n ********************************");
				packet_number++;
			}
			
			/**
			 * Check to see if we need to re-transmit
			 */
			for (int i = 0; i < acknowledged_packets.size(); i++) {
				Packet current_packet = acknowledged_packets.get(i);
				if (current_packet.time_sent + retry_timeout <= System.currentTimeMillis()) {
					System.out.println("re-sending " + current_packet.number);
					sending_sock.send(current_packet.data);
					acknowledged_packets.get(i).time_sent = System
							.currentTimeMillis();
					current_packet.time_sent = System.currentTimeMillis();
					acknowledged_packets.remove(i);
					acknowledged_packets.add(current_packet);
				}
			}
			if (acknowledged_packets.size() == 0 ) {
				stop = true;
			}
		}
	}

	public class Acknowledge extends Thread {

		private DatagramSocket recieving_sock = null;

		public Acknowledge(int port_number) throws Exception {
			recieving_sock = new DatagramSocket(port_number + 1);
		}
		
		@Override
		public void run() {
			while (!stop) {
				byte[] buffer = new byte[2];
				DatagramPacket incoming_packet = new DatagramPacket(buffer,
						buffer.length);
				try {
					recieving_sock.setSoTimeout(retry_timeout);
					recieving_sock.receive(incoming_packet);
					byte[] data = incoming_packet.getData();
					acked_packet_num = ByteBuffer.wrap(data).getShort();
					System.out.println("AckedPacket " + acked_packet_num);
				} catch (SocketTimeoutException ste) {
					System.out.println("Socket Timedout waiting for a response");
				} catch (IOException io) {
					System.out.println(io);
				}
				Thread.yield();
			}
		}
	}

	public static void main(String args[]) throws Exception {
		/**
		 * java SenderX localhost <Port> <Filename> [RetryTimeout] [WindowSize] 
		 */
		if (args.length == 5) { // valid arguments, specify host
			String host = args[0];
			int port_number = Integer.parseInt(args[1]);
			String file_name = args[2];
			int retry_timeout = Integer.parseInt(args[3]);
			int window_size = Integer.parseInt(args[4]);
			
			Sender2a sender2a = new Sender2a(host, port_number, file_name,
					retry_timeout, window_size);
			sender2a.send();
		} else { // invalid arguments
			System.out
					.println("Usage: \n"
							+ " java Sender1b localhost <Port> <Filename> [RetryTimeout] [WindowSize]");
		}
	}
	
	public class Packet {
		int number;
		DatagramPacket data;
		long time_sent;
		public Packet(int number, DatagramPacket data, long time_sent) {
			this.number = number;
			this.data = data;
			this.time_sent = time_sent;
		}
	}
}

