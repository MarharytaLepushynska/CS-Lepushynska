import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUDP {
    private final ConcurrentHashMap<Byte, InfoClient> clients = new ConcurrentHashMap<>();
    private final Storage storage = new Storage();

    private final ArrayBlockingQueue<DatagramPacket> packets = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toDecriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toProcessor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toEncriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toSender = new ArrayBlockingQueue<>(50);

    private final StoreServerUDP serverUDP;
    private final StoreServer storeServer;

    public ServerUDP() throws SocketException {
        DatagramSocket socket = new DatagramSocket();

        serverUDP = new StoreServerUDP(packets);

        ReceiverUDP receiver = new ReceiverUDP(packets, toDecriptor, clients, socket);
        SenderUDP sender = new SenderUDP(toSender, clients, socket);

        storeServer = new StoreServer(storage,
                toDecriptor, toProcessor, toEncriptor, toSender,
                List.of(receiver),
                List.of(sender),
                1, 1, 1
        );
    }

    public void start() {
        new Thread(serverUDP).start();
        storeServer.start();
    }

    public void stop() {
        serverUDP.stop();
        storeServer.stop();
    }

    public Storage getStorage() {
        return storage;
    }
}
