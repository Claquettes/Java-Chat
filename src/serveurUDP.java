package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class serveurUDP implements Runnable {
    private static Map<Integer, List<ClientInfo>> rooms = new HashMap<>();
    private final int roomNumber;
    private static DatagramSocket server;
    private static final long TIMEOUT_INTERVAL = 100000; // in milliseconds

    public serveurUDP(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public static void main(String[] args) {
        System.out.println("Serveur UDP started");
        try {
            server = new DatagramSocket(2345, InetAddress.getLocalHost());
            while (true) {
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);
                packet.setLength(buffer.length);

                String str = new String(packet.getData()).trim();
                if (!str.startsWith("FETCH")) {
                    System.out.println("Received message: " + str);
                }

                String[] tokens = str.split(" ");
                String command = tokens[0];
                int roomNumber;
                String message;
                String pseudonym;

                switch (command) {
                    case "ROOM":
                        roomNumber = Integer.parseInt(tokens[1]);
                        pseudonym = tokens[2];
                        rooms.computeIfAbsent(roomNumber, k -> new ArrayList<>())
                                .add(new ClientInfo(packet.getAddress(), packet.getPort(), pseudonym,
                                        System.currentTimeMillis()));
                        System.out.println("Client added to room " + roomNumber);
                        break;
                    case "MSG":
                        roomNumber = Integer.parseInt(tokens[1]);
                        pseudonym = tokens[2];
                        message = str.substring(tokens[0].length() + tokens[1].length() + tokens[2].length() + 3);
                        sendMsgToRoomClients(roomNumber, pseudonym, message, packet.getAddress(), packet.getPort());
                        break;
                    case "FETCH":
                        roomNumber = Integer.parseInt(tokens[1]);
                        sendMessagesToClient(server, roomNumber, packet.getAddress(), packet.getPort());
                        break;
                    case "MSG EXIT":
                        System.out.println("Client asked to leave room");
                        roomNumber = Integer.parseInt(tokens[1]);
                        removeClientFromRoom(roomNumber, packet.getAddress(), packet.getPort());
                        break;
                }

                checkClientTimeouts();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.close();
            }
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

    private static void sendMsgToRoomClients(int roomNumber, String pseudonym, String message,
            InetAddress senderAddress, int senderPort) {
        List<ClientInfo> clients = rooms.get(roomNumber);
        if (clients != null) {
            for (ClientInfo client : clients) {
                if (!client.getAddress().equals(senderAddress) || client.getPort() != senderPort) {
                    byte[] msgBuffer = (pseudonym + " - " + message).getBytes();
                    DatagramPacket msgPacket = new DatagramPacket(msgBuffer, msgBuffer.length,
                            client.getAddress(),
                            client.getPort());
                    try {
                        server.send(msgPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void sendMessagesToClient(DatagramSocket server, int roomNumber, InetAddress clientAddress,
            int clientPort) throws IOException {
        // This method remains unchanged as it's used for client message fetching
    }

    private static void removeClientFromRoom(int roomNumber, InetAddress clientAddress, int clientPort) {
        List<ClientInfo> clients = rooms.get(roomNumber);
        if (clients != null) {
            clients.removeIf(client -> client.getAddress().equals(clientAddress) && client.getPort() == clientPort);
            if (clients.isEmpty()) {
                rooms.remove(roomNumber);
            }
        }
    }

    private static class ClientInfo {
        private InetAddress address;
        private int port;
        private String pseudonym;
        private long lastActiveTime;

        public ClientInfo(InetAddress address, int port, String pseudonym, long lastActiveTime) {
            this.address = address;
            this.port = port;
            this.pseudonym = pseudonym;
            this.lastActiveTime = lastActiveTime;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public String getPseudonym() {
            return pseudonym;
        }

        public long getLastActiveTime() {
            return lastActiveTime;
        }
    }

    private static void checkClientTimeouts() {
        long currentTime = System.currentTimeMillis();
        Iterator<Integer> roomIterator = rooms.keySet().iterator();
        while (roomIterator.hasNext()) {
            Integer roomNumber = roomIterator.next();
            List<ClientInfo> clients = rooms.get(roomNumber);
            if (clients == null) {
                roomIterator.remove();
                continue;
            }

            Iterator<ClientInfo> clientIterator = clients.iterator();
            while (clientIterator.hasNext()) {
                ClientInfo client = clientIterator.next();
                if (currentTime - client.getLastActiveTime() > TIMEOUT_INTERVAL) {
                    clientIterator.remove();
                    System.out.println("Client " + client.getAddress() + ":" + client.getPort() + " timed out");

                }
            }

            if (clients.isEmpty()) {
                roomIterator.remove();
                System.out.println("Room " + roomNumber + " became empty and was removed");
            }
        }
    }
}
