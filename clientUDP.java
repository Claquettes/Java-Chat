package projet_reso;

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
		try {
			DatagramSocket client = new DatagramSocket();
			InetAddress adresse = InetAddress.getLocalHost();
			Scanner scanner = new Scanner(System.in);

			System.out.println("Entrez le numéro de la salle:");
			String roomId = scanner.nextLine();

			// Thread pour l'envoi de messages
			new Thread(() -> {
				try {
					String message;
					do {
						System.out.println("Entrez un message à envoyer au serveur (ou 'EXIT' pour quitter):");
						message = scanner.nextLine();
						byte[] buffer = (roomId + "/" + message).getBytes();
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, 2345);
						client.send(packet);
					} while (!message.equals("EXIT"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

			// Thread pour la réception de messages
			new Thread(() -> {
				try {
					byte[] buffer2 = new byte[8196];
					DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
					while (true) {
						client.receive(packet2);
						System.out.println("Message du serveur: " + new String(packet2.getData(), 0, packet2.getLength()).trim());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}