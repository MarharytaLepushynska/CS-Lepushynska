import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

public class StoreServerTCP implements Runnable {
    private final ArrayBlockingQueue<Socket> sockets;
    private volatile boolean stopped = false;

    public StoreServerTCP(ArrayBlockingQueue<Socket> sockets) {
        this.sockets = sockets;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(8080)){
            while(!stopped){
                Socket socket = serverSocket.accept();
                sockets.put(socket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        stopped = true;
    }
}
