package Implementations;

import Interfaces.Sender;

import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

public class SenderImpl implements Sender, Runnable {
    private ArrayBlockingQueue<byte[]> queueEncr;
    private volatile boolean stopped = false;

    public SenderImpl(ArrayBlockingQueue<byte[]> queueEncr) {
        this.queueEncr = queueEncr;
    }

    @Override
    public void sendMessage(byte[] mess, InetAddress target) {
        System.out.println("Sending message: " + mess + " to " + target.getHostAddress());
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                byte[] mess = queueEncr.take();

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
