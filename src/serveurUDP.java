package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class serveurUDP {
	// Map of room numbers to clients
	private static Map<Integer, List<ClientInfo>> rooms = new HashMap<>();

	public static void main(String[] args) {
		System.out.println("Serveur UDP started");
		try {
			DatagramSocket server = new DatagramSocket(2345, InetAddress.getLocalHost());
			while(true){
				byte[] buffer = new byte[8192];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				server.receive(packet);
				packet.setLength(buffer.length);

				String str = new String(packet.getData()).trim();
				if (str.startsWith("ROOM")) {
					int roomNumber = Integer.parseInt(str.split(" ")[1]);
					rooms.computeIfAbsent(roomNumber, k -> new ArrayList<>()).add(new ClientInfo(packet.getAddress(), packet.getPort()));
					System.out.println("Client added to room " + roomNumber);
				} else if (str.startsWith("MSG")) {
					int roomNumber = Integer.parseInt(str.split(" ")[1]);
					String message = str.split(" ", 3)[2];
					for (ClientInfo client : rooms.get(roomNumber)) {
						byte[] msgBuffer = message.getBytes();
						DatagramPacket msgPacket = new DatagramPacket(msgBuffer, msgBuffer.length, client.getAddress(), client.getPort());
						server.send(msgPacket);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class ClientInfo {
		private InetAddress address;
		private int port;

		public ClientInfo(InetAddress address, int port) {
			this.address = address;
			this.port = port;
		}

		public InetAddress getAddress() {
			return address;
		}

		public int getPort() {
			return port;
		}
	}
}