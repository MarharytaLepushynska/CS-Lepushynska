import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreClientTCP implements Runnable {
    private volatile boolean stopped = false;
    private final InetAddress ip = InetAddress.getLoopbackAddress();
    private final int port = 8080;
    private Socket socket;
    private final ArrayBlockingQueue<String> inQueue;
    private final ArrayBlockingQueue<byte[]> responses = new ArrayBlockingQueue<>(50);
    private final AtomicInteger pktId = new AtomicInteger(0);

    public StoreClientTCP(ArrayBlockingQueue<String> inQueue) {
        this.inQueue = inQueue;
    }

    @Override
    public void run(){
        if(!connect()) return;

        SocketWrapper sw = new SocketWrapper(socket, responses);
        new Thread(sw::read).start();

        while(!stopped) {
            try{
                String request = inQueue.take();
                byte[] packet = createPacket(request);
                sw.send(packet);
            } catch (Exception e) {
                System.out.println("Connection problem:  " + e);
                if(!connect()) return;
                sw = new SocketWrapper(socket, responses);
                new Thread(sw::read).start();
            }
        }
    }

    private boolean connect(){
        while(!stopped){
            try {
                socket = new Socket(ip, port);
                System.out.println("Connected to port " + port);
                return true;
            } catch(Exception e){
                System.out.println("Connect failed, try again in 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return false;
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

    private void stop() {
        stopped = true;
    }
}
