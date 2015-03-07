package assignment;

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
/**
 * Matric Number s1210313
 */
public class Reciever1a {

	public static final int MAXIMUM_PACKET_SIZE = 1024;
	public static final String HOST = "localhost";

	private String file_name;
	
	private int port_number;
	private DatagramSocket sock;

	private boolean debug = true;


	public Reciever1a(int port_number, String file_name) {
		this.port_number = port_number;
		this.file_name = file_name;
	}

	public void recieve() {
		System.out.println("server starting");
		try {
			// Open a datagram socket on the specified port number to listen for packets
			sock = new DatagramSocket(port_number);
			
			// the maximum size of a buffer.
			byte[] buffer = new byte[MAXIMUM_PACKET_SIZE]; 
			
			// where the file will be stored and the file writed
			File file = new File(file_name);
			FileOutputStream file_output_stream = new FileOutputStream(file);

			// the recieved packet
			DatagramPacket incoming_packet = 
					new DatagramPacket(buffer,buffer.length);

			// a count of the number of bytes stored for debug
			int num_bytes_recieved = 0;

			// until we've seen the byte flag
			while (true) {
				
				// recieve a new packet
				sock.receive(incoming_packet);
				byte[] data = incoming_packet.getData();

				// get the header from the most recent packet
				byte[] header = Arrays.copyOfRange(data, 0, 2);

				// identify the packet number
				short packet_number = ByteBuffer.wrap(header).getShort();

				// find the flag byte
				int last_packet = (int) data[2];

				// write the packet data to file.
				file_output_stream.write(data, 3, data.length - 3);

				num_bytes_recieved += data.length - 3;

				// debug print statement
				if (debug) {
					System.out.println(" number of bytes recieved: "
							+ num_bytes_recieved
							+ "\n recieved packet number: " + packet_number
							+ "\n of length " + (data.length - 3)
							+ "\n and the bit flag is " + last_packet
							+ "\n ********************************");
				}
				
				// if this is the last packet
				if (last_packet == 1) {
					// finish writing the file and exit
					file_output_stream.close();
					System.exit(1);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String args[]) throws Exception {
		/**
		 * java ReceiverX <Port> <Filename>
		 */
		if(args.length == 2){ // valid arguments, specify host
			
			int port_number = Integer.parseInt(args[0]);
			String file_name = args[1];
			Reciever1a reciever1a = new Reciever1a(port_number, file_name);
			reciever1a.recieve();
		
		}else{ // invalid arguments
			System.out.println(
					"Usage: \n" + 
					"java Receiver1a <Port> <Filename>");
		}
	}
}