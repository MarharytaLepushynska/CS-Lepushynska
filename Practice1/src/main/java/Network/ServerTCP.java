package Network;

import Db.StorageService;
import Implementations.ProcessorDb;
import Tools.Message;
import Tools.Storage;
import Wrappers.StoreServer;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTCP {
    private final ConcurrentHashMap<Byte, SocketWrapper> clients = new ConcurrentHashMap<>();

    private final ArrayBlockingQueue<Socket> sockets = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toDecriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toProcessor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toEncriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toSender = new ArrayBlockingQueue<>(50);

    private final StoreServerTCP storeServerTCP;
    private final StoreServer storeServer;

    public ServerTCP(int decCount, int encCount) {
        storeServerTCP = new StoreServerTCP(sockets);

        ReceiverTCP receiver = new ReceiverTCP(sockets, toDecriptor, clients);
        SenderTCP sender = new SenderTCP(toSender, clients);
        StorageService storageService = new StorageService();
        ProcessorDb processorDb = new ProcessorDb(toProcessor, toEncriptor, storageService);

        storeServer = new StoreServer(toDecriptor, toProcessor, toEncriptor, toSender,
                List.of(receiver),
                List.of(sender),
                List.of(processorDb), decCount, encCount);
    }

    public void start() {
        new Thread(storeServerTCP).start();
        storeServer.start();
    }

    public void stop() {
        storeServerTCP.stop();
        storeServer.stop();
    }

    public ConcurrentHashMap<Byte, SocketWrapper> getClients() {
        return clients;
    }
}
