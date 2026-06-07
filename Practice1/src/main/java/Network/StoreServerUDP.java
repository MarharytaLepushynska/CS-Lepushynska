package Network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;

public class StoreServerUDP implements Runnable {
    private final ArrayBlockingQueue<DatagramPacket> packets;
    private volatile boolean stopped = false;
    private static final int port = 8081;

    public StoreServerUDP(ArrayBlockingQueue<DatagramPacket> packets) {
        this.packets = packets;
    }


    @Override
    public void run() {
        try(DatagramSocket socket = new DatagramSocket(port)) {
            while (!stopped) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                packets.put(packet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        stopped = true;
    }
}
