package src;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class clientUDP {

    private static final int SERVER_PORT = 2345;
    private static String pseudonym;

    public static void main(String[] args) {
        System.out.println("Client UDP");
        final DatagramSocket[] clientContainer = new DatagramSocket[1];
        try {
            clientContainer[0] = new DatagramSocket();
            DatagramSocket client = clientContainer[0];
            InetAddress serverAddress = InetAddress.getLocalHost();
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter your pseudonym:");
            pseudonym = scanner.nextLine();

            System.out.println("Enter room number:");
            String roomNumber = scanner.nextLine();

            while (roomNumber.length() == 0 || !roomNumber.matches("[0-9]+")) {
                System.out.println("Enter a valid room number:");
                roomNumber = scanner.nextLine();
            }

            byte[] roomBuffer = ("ROOM " + roomNumber + " " + pseudonym).getBytes();
            DatagramPacket roomPacket = new DatagramPacket(roomBuffer, roomBuffer.length, serverAddress, SERVER_PORT);
            client.send(roomPacket);

            Thread receiveThread = new Thread(() -> receiveMessages(clientContainer[0]));
            receiveThread.start();

            while (true) {
                System.out.println("Enter a message to send to the server:");
                String message = scanner.nextLine();
                sendMsg(client, serverAddress, roomNumber, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientContainer[0] != null) {
                clientContainer[0].close();
            }
        }
    }

    public static void sendMsg(DatagramSocket client, InetAddress serverAddress, String roomNumber, String message) {
        try {
            if (message.length() == 0) {
                return;
            } else {
                if (message.equalsIgnoreCase("EXIT")) {
                    byte[] buffer = ("MSG EXIT " + roomNumber).getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
                    client.send(packet);
                    System.exit(0);
                } else {
                    byte[] buffer = ("MSG " + roomNumber + " " + pseudonym + " - " + message).getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
                    client.send(packet);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessages(DatagramSocket client) {
        try {
            while (true) {
                byte[] buffer = new byte[8196];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                client.receive(packet);

                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                if (!receivedMessage.isEmpty()) {
                    System.out.println(receivedMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}