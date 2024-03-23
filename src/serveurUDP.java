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
                                .add(new ClientInfo(packet.getAddress(), packet.getPort(), pseudonym, System.currentTimeMillis()));
                        System.out.println("Client added to room " + roomNumber);
                        serveurUDP roomServer = new serveurUDP(roomNumber);
                        Thread roomThread = new Thread(roomServer); // creating the room in a separate thread
                        roomThread.start();
                        break;
                    case "MSG":
                        roomNumber = Integer.parseInt(tokens[1]);
                        pseudonym = tokens[2];
                        message = str.substring(tokens[0].length() + tokens[1].length() + tokens[2].length() + 3); // Extracting message after removing command, roomNumber, and pseudonym
                        for (ClientInfo client : rooms.get(roomNumber)) {
                            if (client.getAddress().equals(packet.getAddress())
                                    && client.getPort() == packet.getPort()) {
                                client.setLastActiveTime(System.currentTimeMillis());
                                continue;
                            }
                            byte[] msgBuffer = (pseudonym + " - " + message).getBytes();
                            DatagramPacket msgPacket = new DatagramPacket(msgBuffer, msgBuffer.length,
                                    client.getAddress(),
                                    client.getPort());
                            server.send(msgPacket);
                        }
                        break;
                    case "FETCH":
                        roomNumber = Integer.parseInt(tokens[1]);
                        sendMessagesToClient(server, roomNumber, packet.getAddress(), packet.getPort());
                        break;
                    case "MSG EXIT":
                        System.out.println("Client asked to leave room");
                        roomNumber = Integer.parseInt(tokens[1]);
                        Iterator<ClientInfo> iterator = rooms.get(roomNumber).iterator();
                        while (iterator.hasNext()) {
                            ClientInfo client = iterator.next();
                            if (client.getAddress().equals(packet.getAddress())
                                    && client.getPort() == packet.getPort()) {
                                iterator.remove();
                                break;
                            }
                        }
                        if (rooms.get(roomNumber).isEmpty()) {
                            rooms.remove(roomNumber);
                        }
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

    private static void sendMessagesToClient(DatagramSocket server, int roomNumber, InetAddress clientAddress,
                                              int clientPort) throws IOException {
        if (rooms.containsKey(roomNumber)) {
            StringBuilder messages = new StringBuilder();
            for (ClientInfo client : rooms.get(roomNumber)) {
                messages.append(client.getPseudonym()).append(" - ").append(client.getMessageBuffer()).append("\n");
                client.clearMessageBuffer(); // Clearing the message buffer after sending
            }
            byte[] msgBuffer = messages.toString().getBytes();
            DatagramPacket msgPacket = new DatagramPacket(msgBuffer, msgBuffer.length, clientAddress, clientPort);
            server.send(msgPacket);
        }
    }

    private static class ClientInfo {
        private InetAddress address;
        private int port;
        private String pseudonym;
        private StringBuilder messageBuffer;
        private long lastActiveTime;

        public ClientInfo(InetAddress address, int port, String pseudonym, long lastActiveTime) {
            this.address = address;
            this.port = port;
            this.pseudonym = pseudonym;
            this.messageBuffer = new StringBuilder();
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

        public StringBuilder getMessageBuffer() {
            return messageBuffer;
        }

        public void appendToMessageBuffer(String message) {
            this.messageBuffer.append(message);
        }

        public long getLastActiveTime() {
            return lastActiveTime;
        }

        public void setLastActiveTime(long lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
        }

        public void clearMessageBuffer() {
            this.messageBuffer.setLength(0);
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
