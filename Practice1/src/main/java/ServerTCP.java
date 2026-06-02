import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTCP {
    private final ConcurrentHashMap<Byte, SocketWrapper> clients = new ConcurrentHashMap<>();
    private final Storage storage  = new Storage();

    private final ArrayBlockingQueue<Socket> sockets = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toDecriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toProcessor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toEncriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toSender = new ArrayBlockingQueue<>(50);

    private final StoreServerTCP storeServerTCP;
    private final StoreServer storeServer;

    public ServerTCP(int decCount, int prosCount, int encCount) {
        storeServerTCP = new StoreServerTCP(sockets);

        ReceiverTCP receiver = new ReceiverTCP(sockets, toDecriptor, clients);
        SenderTCP sender = new SenderTCP(toSender, clients);

        storeServer = new StoreServer(storage,
                toDecriptor, toProcessor, toEncriptor, toSender,
                List.of(receiver),
                List.of(sender),
                decCount, prosCount, encCount);
    }

    public void start() {
        new Thread(storeServerTCP).start();
        storeServer.start();
    }

    public void stop() {
        storeServerTCP.stop();
        storeServer.stop();
    }

    public Storage getStorage() {
        return storage;
    }
}
