package Network;

import Interfaces.Sender;

import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class SenderTCP implements Sender, Runnable {
    private final ArrayBlockingQueue<byte[]> toSender;
    private final ConcurrentHashMap<Byte, SocketWrapper> clientsTable;
    private volatile boolean stopped = false;

    public SenderTCP(ArrayBlockingQueue<byte[]> toSender, ConcurrentHashMap<Byte, SocketWrapper> clientsTable) {
        this.toSender = toSender;
        this.clientsTable = clientsTable;
    }

    @Override
    public void sendMessage(byte[] mess, InetAddress target) {
        try {
            byte bSrc = mess[1];
            SocketWrapper socketWrapper = clientsTable.get(bSrc);
            if (socketWrapper != null) {
                socketWrapper.send(mess);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                byte[] mess = toSender.take();
                sendMessage(mess, InetAddress.getLoopbackAddress());
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e + "Thread was interrupted");
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
