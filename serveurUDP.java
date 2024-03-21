package projet_reso;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetSocketAddress;

import java.util.ArrayList;
import java.net.InetAddress;
import java.util.List;

public class serveurUDP {
	public static void main(String[] args) {
		System.out.println("Serveur UDP");
		try {
			DatagramSocket server = new DatagramSocket(2345);
			byte[] buffer = new byte[8196];
			List<Room> rooms = new ArrayList<>();

			while (true) {
				System.out.println("En attente d'un client...");
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				server.receive(packet);
				new Thread(new ClientHandler(server, packet, rooms)).start();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ClientHandler implements Runnable {
	private DatagramSocket server;
	private DatagramPacket packet;
	private List<Room> rooms;

	public ClientHandler(DatagramSocket server, DatagramPacket packet, List<Room> rooms) {
		this.server = server;
		this.packet = packet;
		this.rooms = rooms;
	}

	@Override
	public void run() {
		System.out.println("Nouveau client");
		try {
			// Conversion des données reçues en chaîne de caractères
			String str = new String(packet.getData(), 0, packet.getLength()).trim();
			String[] parts = str.split("/", 2);
			String roomId = parts[0];
			String message = parts.length > 1 ? parts[1] : "";

			System.out.println("Message reçu: " + message);
			System.out.println("Salon: " + roomId);

			// Recherche du salon avec l'identifiant roomId
			Room room = null;
			for (Room r : rooms) {
				if (r.getId().equals(roomId)) {
					room = r;
					break;
				}
			}

			// Si le salon n'existe pas, création d'un nouveau salon
			if (room == null) {
				room = new Room(roomId);
				rooms.add(room);
			}

			// Ajout du client au salon
			room.addClient(new InetSocketAddress(packet.getAddress(), packet.getPort()));

			// Envoi du message à tous les clients du salon
			byte[] buffer2 = message.getBytes();
			for (InetSocketAddress client : room.getClients()) {
				if (!client.equals(new InetSocketAddress(packet.getAddress(), packet.getPort()))) {
					DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, client.getAddress(), client.getPort());
					server.send(packet2);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}