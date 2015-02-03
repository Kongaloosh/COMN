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
	private DatagramSocket sock;
	
	private boolean debug = true;
	
	public reciever1b(String host_name, int port_number){
		this.port_number = port_number;
	}
	
	public void recieve(){
		try {
			sock = new DatagramSocket(port_number);
			
			byte[] buffer = new byte[MAXIMUM_PACKET_SIZE]; // the maximum size of a buffer.
			
			File file = new File("recieved_image.jpg");
			FileOutputStream file_output_stream = new FileOutputStream(file);
			
			DatagramPacket incoming_packet = new DatagramPacket(buffer, buffer.length);
			
			int num_bytes_recieved = 0;
			
			while (true) {
				sock.receive(incoming_packet);
				byte[]data = incoming_packet.getData();

				// get the header from the most recent packet
				byte[] header = Arrays.copyOfRange(data, 0, 2);
				
				// identify the packet number
				short packet_number = ByteBuffer.wrap(header).getShort();
				
				// find the flag byte
				int last_packet = (int) data[2];
				
				// write the packet data to file. 
				file_output_stream.write(data, 3, data.length-3);
				
				num_bytes_recieved += data.length-3;
				
				if(debug){
					System.out.println(
							" number of bytes recieved: " + num_bytes_recieved +
							"\n recieved packet number: " + packet_number +
							"\n of length " + (data.length-3) + 
							"\n and the bit flag is " + last_packet +
							"\n ********************************");
				}
				
				send_acknowledgement();
				
				if(last_packet == 1){
					file_output_stream.close();
					System.exit(1);
				}
				
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void send_acknowledgement() {
		byte[] data = new byte[1]; // add the flag to identify if it's the last packet
		data[0] = (byte) 1;
		DatagramPacket datagram_packet = new DatagramPacket(data, data.length, host, port_number);
		sock.send(datagram_packet);
	}
	
	public static void main(String args[]) throws Exception {
		echo("out");
		reciever1b reciever1b = new reciever1b(HOST, 7777);
		System.out.println("server starting");
		reciever1b.recieve();
		}
	
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
	}
