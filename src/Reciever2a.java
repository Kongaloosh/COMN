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
 * Matric Number: s1210313
 */
public class Reciever2a {

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
	

	public Reciever2a(int port_number, String file_name) {
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
			DatagramPacket incoming_packet = new DatagramPacket(buffer,	buffer.length);
			int num_bytes_recieved = 0;
			int last_packet_number = -1;

			while (true) {
				recieving_sock.receive(incoming_packet);
				byte[] data = incoming_packet.getData();
				
				try {
					host = incoming_packet.getAddress();
				} catch (Exception e) {
					System.out.print(e);
				}

				byte[] header = Arrays.copyOfRange(data, 0, 2);
				short packet_number = ByteBuffer.wrap(header).getShort();

				if (last_packet_number >= packet_number){
					send_acknowledgement(packet_number);
				}else if (last_packet_number+1 == packet_number){
					
					int last_packet = (int) data[2];
					file_output_stream.write(data, 3, data.length - 3);
					num_bytes_recieved += data.length - 3;
					
					if (debug) {
						System.out.println(" number of bytes recieved: "
								+ num_bytes_recieved
								+ "\n recieved packet number: " + packet_number
								+ "\n of length " + (data.length - 3)
								+ "\n and the bit flag is " + last_packet
								+ "\n ********************************");
					}
					
					send_acknowledgement(packet_number);

					if (last_packet == 1) {
						file_output_stream.close();
						System.exit(1);
					}
					last_packet_number = packet_number;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void send_acknowledgement(short packet_number) throws Exception {
		byte[] data = new byte[2]; 
		data = ByteBuffer.allocate(2).putShort(packet_number).array();
		DatagramPacket datagram_packet = new DatagramPacket(data, data.length, host, port_number+1);
		sending_sock.send(datagram_packet);
		System.out.println("acking: " + packet_number);
	}

	public static void main(String args[]) throws Exception {
		/**
		 * java ReceiverX <Port> <Filename>
		 */
		if(args.length == 2){ // valid arguments, specify host
			
			int port_number = Integer.parseInt(args[0]);
			String file_name = args[1];
			Reciever2a reciever1b = new Reciever2a(port_number, file_name);
			reciever1b.recieve();
		
		}else{ // invalid arguments
			System.out.println(
					"Usage: \n" + 
					"java Receiver1b <Port> <Filename>");
		}
	}
}