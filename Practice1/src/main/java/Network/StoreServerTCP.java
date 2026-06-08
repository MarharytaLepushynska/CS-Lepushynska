package Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

public class StoreServerTCP implements Runnable {
    private final ArrayBlockingQueue<Socket> sockets;
    private volatile boolean stopped = false;
    private ServerSocket serverSocket;

    public StoreServerTCP(ArrayBlockingQueue<Socket> sockets) {
        this.sockets = sockets;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8080);

            while(!stopped){
                try {
                    Socket socket = serverSocket.accept();
                    sockets.put(socket);
                } catch (IOException e) {
                    if(stopped) break;
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        stopped = true;

        try {
            if(serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
