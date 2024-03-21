import src.serveurUDP;
import src.clientUDP;

public class Main {
    public static void main(String[] args) {
        // Start the server in a new thread
        new Thread(() -> serveurUDP.main(args)).start();

        // Wait for the server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start two clients in new threads
        new Thread(() -> clientUDP.main(args)).start();
        new Thread(() -> clientUDP.main(args)).start();
    }
} 
