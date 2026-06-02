import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreClientUDP implements Runnable {
    private final ArrayBlockingQueue<String> inQueue;
    private final ArrayBlockingQueue<Long> repQueue = new ArrayBlockingQueue<>(50);
    private final AtomicInteger pktId = new AtomicInteger(0);
    private static final int serverPort = 8081;
    private volatile boolean stopped = false;

    public StoreClientUDP(ArrayBlockingQueue<String> inQueue) {
        this.inQueue = inQueue;
    }

    @Override
    public void run() {
        try(DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getLoopbackAddress();

            new Thread(() -> waitAck(socket)).start();

            while(!stopped) {
                String command = inQueue.take();
                byte[] packet = createPacket(command);
                long id = pktId.get();

                boolean replied = false;
                while(!replied && !stopped) {
                    DatagramPacket reply = new DatagramPacket(packet, packet.length, address, serverPort);
                    socket.send(reply);

                    Long waitedRep = repQueue.poll(5, TimeUnit.SECONDS);
                    if(waitedRep != null && waitedRep == id) {
                        replied = true;
                    } else {
                        System.out.println("Connection failed, waiting for reply");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void waitAck(DatagramSocket socket) {
        while(!stopped) {
            try {
                byte[] buf = new byte[10];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                long receiveId = ByteBuffer.wrap(buf, 2, 8).getLong();
                repQueue.put(receiveId);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private byte[] createPacket(String in){
        String[] parts = in.split(" ");
        int cType = Integer.parseInt(parts[0]);
        String message = in.substring(in.indexOf(" ") + 1);
        Message msg = new Message((byte) 0x10,
                pktId.incrementAndGet(),
                cType,
                4,
                message);
        try {
            Encoder encoder = new Encoder();
            return encoder.encode(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
