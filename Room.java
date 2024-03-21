package projet_reso;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String id;
    private List<InetSocketAddress> clients;

    public Room(String id) {
        this.id = id;
        this.clients = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void addClient(InetSocketAddress client) {
        clients.add(client);
    }

    public List<InetSocketAddress> getClients() {
        return clients;
    }

    public void removeClient(InetSocketAddress client) {
        clients.remove(client);
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }
}