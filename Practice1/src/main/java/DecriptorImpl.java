import java.util.concurrent.ArrayBlockingQueue;

public class DecriptorImpl implements Decriptor, Runnable {
    private ArrayBlockingQueue<byte[]> queueRec;
    private ArrayBlockingQueue<Message> queueProc;
    private volatile boolean stopped = false;
    private Decoder decoder;

    DecriptorImpl(ArrayBlockingQueue<byte[]> queueRec, ArrayBlockingQueue<Message> queueProc) {
        this.queueRec = queueRec;
        this.queueProc = queueProc;
        decoder = new Decoder();
    }

    @Override
    public void decrypt(byte[] message) {
        try {
            Message mes = decoder.decode(message);
            queueProc.put(mes);
        } catch (Exception e) {
            throw new RuntimeException(e + "Decryption failed");
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                byte[] message = queueRec.take();
                decrypt(message);
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
