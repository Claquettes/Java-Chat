package src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class clientUDP {

	private static final int SERVER_PORT = 2345;
	private static final int FETCH_INTERVAL_MS = 1000;

	public static void main(String[] args) {
		System.out.println("Client UDP");
		try {
			DatagramSocket client = new DatagramSocket();
			InetAddress serverAddress = InetAddress.getLocalHost();
			Scanner scanner = new Scanner(System.in);
			System.out.println("Enter room number:");
			String roomNumber = scanner.nextLine();
			byte[] roomBuffer = ("ROOM " + roomNumber).getBytes();
			DatagramPacket roomPacket = new DatagramPacket(roomBuffer, roomBuffer.length, serverAddress, SERVER_PORT);
			client.send(roomPacket);
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new FetchMessagesTask(client, serverAddress, roomNumber), 0, FETCH_INTERVAL_MS);
			while (true) {
				System.out.println("Enter a message to send to the server:");
				String message = scanner.nextLine();
				sendMsg(client, serverAddress, roomNumber, message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendMsg(DatagramSocket client, InetAddress serverAddress, String roomNumber, String message) {
		try {
			if (message.length() == 0) {
				return;
			} else {
				if (message.startsWith("EXIT") || message.equals("EXIT") || message.equals("exit")) {
					byte[] buffer = ("EXIT " + roomNumber).getBytes();
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
					client.send(packet);
					System.exit(0);
				} else {
					byte[] buffer = ("MSG " + roomNumber + " " + message).getBytes();
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
					client.send(packet);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class FetchMessagesTask extends TimerTask {
		private DatagramSocket client;
		private InetAddress serverAddress;
		private String roomNumber;

		public FetchMessagesTask(DatagramSocket client, InetAddress serverAddress, String roomNumber) {
			this.client = client;
			this.serverAddress = serverAddress;
			this.roomNumber = roomNumber;
		}

		@Override
		public void run() {
			try {
				byte[] fetchBuffer = ("FETCH " + roomNumber).getBytes();
				DatagramPacket fetchPacket = new DatagramPacket(fetchBuffer, fetchBuffer.length, serverAddress,
						SERVER_PORT);
				client.send(fetchPacket);

				byte[] buffer = new byte[8196];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				client.receive(packet);

				String receivedMessage = new String(packet.getData(), 0, packet.getLength());
				if (receivedMessage != null && receivedMessage.length() > 0) {
					System.out.println(receivedMessage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
