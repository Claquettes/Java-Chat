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
			String message;
			do {
				System.out.println("Entrez un message à envoyer au serveur (ou 'EXIT' pour quitter):");
				message = scanner.nextLine();
				byte[] buffer = message.getBytes();
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, 2345);
				client.send(packet);

				byte[] buffer2 = new byte[8196];
				DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
				client.receive(packet2);

				System.out.println("Message du serveur: " + new String(packet2.getData(), 0, packet2.getLength()).trim());
			} while (!message.equals("EXIT"));

			scanner.close();
			client.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}