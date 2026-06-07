package Network;

import Interfaces.Receiver;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ReceiverTCP implements Receiver, Runnable {
    private final ArrayBlockingQueue<Socket> sockets;
    private final ArrayBlockingQueue<byte[]> toDecriptor;
    private final ConcurrentHashMap<Byte, SocketWrapper> clientsTable;
    private volatile boolean stopped = false;

    public ReceiverTCP(ArrayBlockingQueue<Socket> sockets, ArrayBlockingQueue<byte[]> toDecriptor, ConcurrentHashMap<Byte, SocketWrapper> clientsTable) {
        this.sockets = sockets;
        this.toDecriptor = toDecriptor;
        this.clientsTable = clientsTable;
    }

    @Override
    public void receiveMessage(byte[] message) {
        try {
            toDecriptor.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try{
                Socket socket = sockets.take();
                SocketWrapper sw = new SocketWrapper(socket, toDecriptor, clientsTable);
                new Thread(sw::read).start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
