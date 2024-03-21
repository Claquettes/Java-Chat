package src;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class serveurUDP {
	public static void main(String[] args) {
		System.out.println("Serveur UDP");
		try {
			// Création d'un nouveau socket serveur sur le port 2345
			DatagramSocket server = new DatagramSocket(2345, InetAddress.getLocalHost());
			while(true){
				// Création d'un buffer pour stocker les données entrantes
				byte[] buffer = new byte[8192];
				// Création d'un paquet pour recevoir les données
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				// Attente de la réception d'un paquet
				server.receive(packet);
				packet.setLength(buffer.length);

				// Conversion des données reçues en chaîne de caractères
				String str = new String(packet.getData()).trim();
				// Si le message reçu est "hello serveur RX302"
				if (str.equals("hello serveur RX302")) {
					System.out.println("Nouveau client : " + packet.getAddress() + ":" + packet.getPort());
					//We open a new thread to handle the client
					new Thread(() -> {
						try {
							int id = (int) (Math.random() * 1000);
							// Création d'un message de réponse

							//we concatenate the message with the id
							String Welcome_message = "You have been assigned the ID: " + id + ". Serveur RX302 ready";
							byte[] buffer2 = Welcome_message.getBytes();
							// Création d'un paquet pour envoyer la réponse
							DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, packet.getAddress(), packet.getPort());
							// Envoi de la réponse
							server.send(packet2);
						} catch (IOException e) {
							// Gestion des exceptions liées aux entrées/sorties
							e.printStackTrace();
						}
					}).start();
				} else {
					// Si le message reçu n'est pas "hello serveur RX302", affichage du message et renvoi du même message
					System.out.println("Message from client: " + str);
					byte[] buffer2 = str.getBytes();
					DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, packet.getAddress(), packet.getPort());
					server.send(packet2);
				}
			}
		} catch (SocketException e) {
			// Gestion des exceptions liées au socket
			e.printStackTrace();
		} catch (IOException e) {
			// Gestion des exceptions liées aux entrées/sorties
			e.printStackTrace();
		}
	}
}