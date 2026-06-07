package Interfaces;

public interface Receiver extends Runnable {
    void receiveMessage(byte[] message);

    void stop();
}
