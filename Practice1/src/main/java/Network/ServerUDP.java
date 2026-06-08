package Network;

import Db.StorageService;
import Implementations.ProcessorDb;
import Tools.Message;
import Wrappers.StoreServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUDP {
    private final ConcurrentHashMap<Byte, InfoClient> clients = new ConcurrentHashMap<>();

    private final ArrayBlockingQueue<DatagramPacket> packets = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toDecriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toProcessor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<Message> toEncriptor = new ArrayBlockingQueue<>(50);
    private final ArrayBlockingQueue<byte[]> toSender = new ArrayBlockingQueue<>(50);

    private final StoreServerUDP serverUDP;
    private final StoreServer storeServer;

    public ServerUDP(int decCount, int encCount) throws SocketException {
        DatagramSocket socket = new DatagramSocket();

        serverUDP = new StoreServerUDP(packets);

        ReceiverUDP receiver = new ReceiverUDP(packets, toDecriptor, clients, socket);
        SenderUDP sender = new SenderUDP(toSender, clients, socket);
        StorageService storageService = new StorageService();
        ProcessorDb processorDb = new ProcessorDb(toProcessor, toEncriptor, storageService);

        storeServer = new StoreServer(toDecriptor, toProcessor, toEncriptor, toSender,
                List.of(receiver),
                List.of(sender),
                List.of(processorDb), decCount, encCount);
    }

    public void start() {
        new Thread(serverUDP).start();
        storeServer.start();
    }

    public void stop() {
        serverUDP.stop();
        storeServer.stop();
    }
}
