package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class clientUDP {
	private static int id;
	private static int port;

	public static void setId(int id) {
		clientUDP.id = id;
		System.out.println("Client Id successfully set to " + id);
	}

	public static void setPort(int port) {
		clientUDP.port = port;
		System.out.println("Client port successfully set to " + port);
	}

	public int getId() {
		return this.id;
	}

	public int getPort() {
		return this.port;
	}

	public static void main(String[] args) {
		System.out.println("Client UDP");
		// Création du message initial à envoyer au serveur
		byte[] buffer = "hello serveur RX302".getBytes();
		try {
			// Création d'un nouveau socket client
			DatagramSocket client = new DatagramSocket();
			// Récupération de l'adresse locale
			InetAddress adresse = InetAddress.getLocalHost();
			// Création d'un paquet pour envoyer le message initial
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, 2345);
			// Envoi du paquet
			client.send(packet);

			// Création d'un buffer pour recevoir la réponse du serveur
			byte[] buffer2 = new byte[8196];
			// Création d'un paquet pour recevoir la réponse
			DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, adresse, 2345);
			// Attente de la réponse du serveur
			client.receive(packet2);

			// Affichage de la réponse du serveur
			System.out.println(
					new String(packet2.getData()).trim() + " : " + packet2.getAddress() + ":" + packet2.getPort());

			if (new String(packet2.getData()).trim().startsWith("You")) {
				System.out.println("DESPACITO");
				String serverResponse = new String(packet2.getData(), 0, packet2.getLength()).trim();
				// Parse the ID and port from the server's response
				String[] words = serverResponse.split(" ");
				try {
					setId(Integer.parseInt(words[6]));

					System.out.println("Trying to parse port from " + words[10]);
					setPort(Integer.parseInt(words[10]));
				} catch (NumberFormatException e) {
					System.out.println("Could not parse ID and port from server's response.");
				}
				// Création d'un scanner pour lire l'entrée de l'utilisateur
				Scanner scanner = new Scanner(System.in);
				System.out.println("Entrez un message à envoyer au serveur:");
				String message = scanner.nextLine();
				// Conversion du message en bytes
				byte[] buffer3 = message.getBytes();
				// Création d'un paquet pour envoyer le message
				DatagramPacket packet3 = new DatagramPacket(buffer3, buffer3.length, adresse, 2345);
				// Envoi du message
				client.send(packet3);

				// Création d'un buffer pour recevoir la réponse du serveur
				byte[] buffer4 = new byte[8196];
				// Création d'un paquet pour recevoir la réponse
				DatagramPacket packet4 = new DatagramPacket(buffer4, buffer4.length, adresse, 2345);
				// Attente de la réponse du serveur
				client.receive(packet4);

				// Affichage de la réponse du serveur
				System.out.println("Message du serveur: " + new String(packet4.getData()).trim());

				scanner.close();
			}
			// Fermeture du socket client
			client.close();
		} catch (SocketException e) {
			// Gestion des exceptions liées au socket
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// Gestion des exceptions liées à l'hôte inconnu
			e.printStackTrace();
		} catch (IOException e) {
			// Gestion des exceptions liées aux entrées/sorties
			e.printStackTrace();
		}
	}
}