package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class clientUDP {
	public static void main(String[] args){
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
			System.out.println(new String(packet2.getData()).trim() + " : " + packet2.getAddress() + ":" + packet2.getPort());

			// Si la réponse du serveur est "Serveur RX302 ready"
			if (new String(packet2.getData()).trim().equals("Serveur RX302 ready")) {
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