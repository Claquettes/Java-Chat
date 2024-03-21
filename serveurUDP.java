package projet_reso;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetSocketAddress;

import java.util.ArrayList;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;


public class serveurUDP {
    public static void main(String[] args) {
        System.out.println("Serveur UDP");
        try {
            DatagramSocket server = new DatagramSocket(2345);
            byte[] buffer = new byte[8196];
            List<Room> rooms = new ArrayList<>();
            Map<InetSocketAddress, ClientHandler> clients = new HashMap<>();

            while (true) {
                System.out.println("En attente d'un client...");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);
                InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());

                if (!clients.containsKey(clientAddress)) {
                    ClientHandler handler = new ClientHandler(server, rooms, clientAddress);
                    clients.put(clientAddress, handler);
                    new Thread(handler).start();
                }

                clients.get(clientAddress).addPacket(packet);
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
	private List<Room> rooms;
	private InetSocketAddress clientAddress;
	private Queue<DatagramPacket> packets = new LinkedList<>();

	public ClientHandler(DatagramSocket server, List<Room> rooms, InetSocketAddress clientAddress) {
		this.server = server;
		this.rooms = rooms;
		this.clientAddress = clientAddress;
	}

	public void addPacket(DatagramPacket packet) {
		packets.add(packet);
	}

	@Override
	public void run() {
		try {
			Room room = null;
			String message = "";
			while (true) {
				if (packets.isEmpty()) {
					continue;
				}

				DatagramPacket packet = packets.poll();
				String str = new String(packet.getData(), 0, packet.getLength()).trim();
				String[] parts = str.split("/", 2);
				String roomId = parts[0];
				message = parts.length > 1 ? parts[1] : "";

				System.out.println("Message reçu: " + message);
				System.out.println("Salon: " + roomId);

				// Recherche du salon avec l'identifiant roomId
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
				room.addClient(clientAddress);

				// Envoi du message à tous les clients du salon
				byte[] buffer2 = message.getBytes();
				for (InetSocketAddress client : room.getClients()) {
					if (!client.equals(clientAddress)) {
						DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, client.getAddress(), client.getPort());
						server.send(packet2);
					}
				}

				if (message.equals("quit")) {
					room.removeClient(clientAddress);
					if (room.isEmpty()) {
						rooms.remove(room);
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}