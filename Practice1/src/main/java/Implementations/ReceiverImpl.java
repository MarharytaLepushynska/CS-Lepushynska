package Implementations;

import Interfaces.Receiver;
import Tools.Encoder;
import Tools.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceiverImpl implements Receiver, Runnable {
    private ArrayBlockingQueue<String> inQueueTest;
    private ArrayBlockingQueue<byte[]> queue;
    private volatile boolean stopped = false;
    private final AtomicInteger bPktId;

    public ReceiverImpl(ArrayBlockingQueue<String> inQueueTest, ArrayBlockingQueue<byte[]> queue) {
        this.inQueueTest = inQueueTest;
        this.queue = queue;
        bPktId = new AtomicInteger();
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
            String in = inQueueTest.take();
            byte[] message = generateRandomMessage(in);
            receiveMessage(message);
            Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e + "Receiving failed");
            }
        }
    }

    @Override
    public void receiveMessage(byte[] message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e + "Thread was interrupted");
        }
    }

    public void stop() {
        stopped = true;
    }

    private byte[] generateRandomMessage(String in) {
        String[] parts = in.split(" ");
        int cType = Integer.parseInt(parts[0]);
        String message = in.substring(in.indexOf(" ") + 1);
        int pktId = bPktId.incrementAndGet();
        byte bSrc = (byte)0x10;
        int bUserId = 4;

        Message encodedPacket = new Message(bSrc, pktId, cType, bUserId, message);
        Encoder encoder = new Encoder();
        byte[] packet = null;
        try {
            packet = encoder.encode(encodedPacket);
        } catch (Exception e){
            throw new RuntimeException(e + "Exception while encoding packet");
        }
        return packet;
    }
}
