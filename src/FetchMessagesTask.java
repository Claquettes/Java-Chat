package src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TimerTask;

public class FetchMessagesTask extends TimerTask {
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
                    client.getLocalPort());
            client.send(fetchPacket);

            byte[] buffer = new byte[8196];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);

            String receivedMessage = new String(packet.getData(), 0, packet.getLength());
            if (receivedMessage != null && receivedMessage.length() > 0 && !receivedMessage.equals(" - ")) {
                System.out.println(receivedMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
