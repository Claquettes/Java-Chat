package src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class clientUDP {
	public static void main(String[] args) {
		System.out.println("Client UDP");
		try {
			DatagramSocket client = new DatagramSocket();
			InetAddress adresse = InetAddress.getLocalHost();

			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter room number:");
			String roomNumber = scanner.nextLine();
			byte[] roomBuffer = ("ROOM " + roomNumber).getBytes();
			DatagramPacket roomPacket = new DatagramPacket(roomBuffer, roomBuffer.length, adresse, 2345);
			client.send(roomPacket);

			while (true) {
				System.out.println("Enter a message to send to the server:");
				String message = scanner.nextLine();
				sendMsg(client, adresse, roomNumber, message);

				byte[] buffer2 = new byte[8196];
				DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, adresse, 2345);
				client.receive(packet2);

				System.out.println("Message from server: " + new String(packet2.getData()).trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendMsg(DatagramSocket client, InetAddress adresse, String roomNumber, String message) {
		try {
			byte[] buffer = ("MSG " + roomNumber + " " + message).getBytes();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, 2345);
			client.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}