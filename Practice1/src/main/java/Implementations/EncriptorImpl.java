package Implementations;

import Interfaces.Encriptor;
import Tools.Encoder;
import Tools.Message;

import java.util.concurrent.ArrayBlockingQueue;

public class EncriptorImpl implements Encriptor, Runnable {
    private ArrayBlockingQueue<Message> queueProc;
    private ArrayBlockingQueue<byte[]> queueSend;
    private volatile boolean stopped = false;
    private Encoder encoder;

    public EncriptorImpl(ArrayBlockingQueue<Message> queueProc, ArrayBlockingQueue<byte[]> queueSend) {
        this.queueProc = queueProc;
        this.queueSend = queueSend;
        encoder = new Encoder();
    }

    @Override
    public void encrypt(Message message) {
        try {
            byte[] encrypted = encoder.encode(message);
            queueSend.put(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e + "Encryption failed");
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Message message = queueProc.take();
                encrypt(message);
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
