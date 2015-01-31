import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Reciever1a {
	
	public static final int MAXIMUM_PACKET_SIZE = 1024;
	
	public static final String HOST = "localhost";
	
	private int port_number;
	
	private DatagramSocket sock;
	
	public Reciever1a(String host_name, int port_number){
		this.port_number = port_number;
	}
	
	public void recieve(){
		try {
			sock = new DatagramSocket(port_number);
			byte[] buffer = new byte[65536];
			DatagramPacket incoming_packet = new DatagramPacket(buffer, buffer.length);
			System.out.println("server created. wating for incoming data");
			
			while (true) {
				sock.receive(incoming_packet);
				byte[]data = incoming_packet.getData();
				String s = new String(data, 0, incoming_packet.getLength());
				
				System.out.println(s);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String args[]) throws Exception {
		echo("out");
		Reciever1a reciever1a = new Reciever1a(HOST, 7777);
		System.out.println("server starting");
		reciever1a.recieve();
		}
	
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
	}
