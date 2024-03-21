import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class serveurUDP {
	public static void main(String[] args) {
		try {
			DatagramSocket server = new DatagramSocket(2345);
			byte[] buffer = new byte[8196];

			while (true) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				server.receive(packet);
				new Thread(new ClientHandler(server, packet)).start();
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

	public ClientHandler(DatagramSocket server, DatagramPacket packet) {
		this.server = server;
		this.packet = packet;
	}

	@Override
	public void run() {
		try {
			// Conversion des données reçues en chaîne de caractères
			String str = new String(packet.getData()).trim();

			// Si le message reçu est "hello serveur RX302"
			if (str.equals("hello serveur RX302")) {
				// Affichage de l'adresse et du port du client
				System.out.println("Nouveau client : " + packet.getAddress() + ":" + packet.getPort());

				// Création d'un message de réponse
				byte[] buffer2 = "Serveur RX302 ready".getBytes();

				// Création d'un paquet pour envoyer la réponse
				DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, packet.getAddress(), packet.getPort());

				// Envoi de la réponse
				server.send(packet2);
			} else {
				// Si le message reçu n'est pas "hello serveur RX302", affichage du message et renvoi du même message
				System.out.println("Message from client: " + str);
				byte[] buffer2 = str.getBytes();
				DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, packet.getAddress(), packet.getPort());
				server.send(packet2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}