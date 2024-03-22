package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class serveurUDP implements Runnable {
    // Map of room numbers to clients
    private static Map<Integer, List<ClientInfo>> rooms = new HashMap<>();
    private final int roomNumber;

    public serveurUDP(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public static void main(String[] args) {
        System.out.println("Serveur UDP started");
        try {
            DatagramSocket server = new DatagramSocket(2345, InetAddress.getLocalHost());
            while (true) {
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);
                packet.setLength(buffer.length);

                String str = new String(packet.getData()).trim();
                System.out.println("Received: " + str);

                if (str.startsWith("ROOM")) {
                    int roomNumber = Integer.parseInt(str.split(" ")[1]);
                    rooms.computeIfAbsent(roomNumber, k -> new ArrayList<>())
                            .add(new ClientInfo(packet.getAddress(), packet.getPort()));
                    System.out.println("Client added to room " + roomNumber);
                    serveurUDP roomServer = new serveurUDP(roomNumber);
                    Thread roomThread = new Thread(roomServer); // on créé la room dans un thread a part
                    roomThread.start();
                } else if (str.startsWith("MSG")) {
                    int roomNumber = Integer.parseInt(str.split(" ")[1]);
                    String message = str.split(" ", 3)[2];
                    for (ClientInfo client : rooms.get(roomNumber)) {
                        if (client.getAddress().equals(packet.getAddress()) && client.getPort() == packet.getPort()) {
                            continue;
                        }
                        byte[] msgBuffer = message.getBytes();
                        DatagramPacket msgPacket = new DatagramPacket(msgBuffer, msgBuffer.length, client.getAddress(),
                                client.getPort());
                        server.send(msgPacket);
                    }
                } else if (str.startsWith("FETCH")) {
                    int roomNumber = Integer.parseInt(str.split(" ")[1]);
                    sendMessagesToClient(server, roomNumber, packet.getAddress(), packet.getPort());
                } else if (str.startsWith("MSG EXIT")) {
                    System.out.println("Client asked to leave room");
                    // we close the connexion of the client and remove it from the room
                    int roomNumber = Integer.parseInt(str.split(" ")[1]);
                    for (ClientInfo client : rooms.get(roomNumber)) {
                        if (client.getAddress().equals(packet.getAddress()) && client.getPort() == packet.getPort()) {
                            rooms.get(roomNumber).remove(client);
                            break;
                        }
                    }
                    if (rooms.get(roomNumber).isEmpty()) {
                        rooms.remove(roomNumber);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Server is running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendMessagesToClient(DatagramSocket server, int roomNumber, InetAddress clientAddress,
            int clientPort) throws IOException {
        if (rooms.containsKey(roomNumber)) {
            StringBuilder messages = new StringBuilder();
            byte[] msgBuffer = messages.toString().getBytes();
            DatagramPacket msgPacket = new DatagramPacket(msgBuffer, msgBuffer.length, clientAddress, clientPort);
            server.send(msgPacket);
        }
    }

    private static class ClientInfo {
        private InetAddress address;
        private int port;

        public ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }
    }
}
