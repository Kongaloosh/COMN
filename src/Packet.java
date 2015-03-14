import java.net.DatagramPacket;

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