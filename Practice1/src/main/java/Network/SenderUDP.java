package Network;

import Interfaces.Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class SenderUDP implements Sender {
    private final ArrayBlockingQueue<byte[]> in;
    private final ConcurrentHashMap<Byte, InfoClient> clients;
    private final DatagramSocket socket;
    private volatile boolean stopped = false;

    public SenderUDP(ArrayBlockingQueue<byte[]> in,
                     ConcurrentHashMap<Byte, InfoClient> clients,
                     DatagramSocket socket) {
        this.in = in;
        this.clients = clients;
        this.socket = socket;
    }

    @Override
    public void sendMessage(byte[] msg, InetAddress target) {
        try {
            byte bSrc = msg[1];
            InfoClient client = clients.get(bSrc);
            if (client == null) {
                DatagramPacket packet = new DatagramPacket(msg, msg.length, client.getAddress(), client.getPort());
                socket.send(packet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while(!stopped) {
            try {
                byte[] msg = in.take();
                sendMessage(msg, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stop() {

    }
}
