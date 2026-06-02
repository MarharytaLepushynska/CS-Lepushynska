import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ReceiverUDP implements Receiver, Runnable {
    private final ArrayBlockingQueue<DatagramPacket> packets;
    private final ArrayBlockingQueue<byte[]> toDecryptor;
    private final ConcurrentHashMap<Byte, InfoClient> clients;
    private final DatagramSocket socket;
    private volatile boolean stopped = false;

    public ReceiverUDP(ArrayBlockingQueue<DatagramPacket> packets, ArrayBlockingQueue<byte[]> toDecryptor, ConcurrentHashMap<Byte, InfoClient> clients, DatagramSocket socket) {
        this.packets = packets;
        this.toDecryptor = toDecryptor;
        this.clients = clients;
        this.socket = socket;
    }

    @Override
    public void receiveMessage(byte[] message) {
        try {
            toDecryptor.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try{
                DatagramPacket packet = packets.take();
                byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                byte bSrc = data[1];
                clients.putIfAbsent(bSrc, new InfoClient(bSrc, packet.getAddress(), packet.getPort()));

                reply(data, packet.getAddress(), packet.getPort());

                receiveMessage(data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void reply(byte[] data, InetAddress address, int port) {
        try{
            byte[] rep = new byte[10];
            rep[0] = data[0];
            rep[1] = data[1];
            System.arraycopy(data, 2, rep, 2, 8);
            DatagramPacket packet = new DatagramPacket(rep, rep.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }
}
