package Network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

class SocketWrapper {
    private final Socket socket;
    private final ArrayBlockingQueue<byte[]> out;
    private final ConcurrentHashMap<Byte, SocketWrapper> clientsTable;

    public SocketWrapper(Socket socket, ArrayBlockingQueue<byte[]> out, ConcurrentHashMap<Byte, SocketWrapper> clientsTable) {
        this.socket = socket;
        this.out = out;
        this.clientsTable = clientsTable;
    }

    public SocketWrapper(Socket socket, ArrayBlockingQueue<byte[]> out) {
        this.socket = socket;
        this.out = out;
        this.clientsTable = null;
    }

    public void send(byte[] packet) {
        try {
            var output = socket.getOutputStream();
            output.write(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void read(){
        try(socket) {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            while(!socket.isClosed()){
                byte[] header = new byte[16];
                dis.readFully(header);

                byte bSrc = header[1];
                if(clientsTable != null) {
                    clientsTable.putIfAbsent(bSrc, this);
                }

                int wLen = ByteBuffer.wrap(header, 10, 4).getInt();

                byte[] packet = new byte[wLen+2];
                dis.readFully(packet);

                byte[] message = new byte[16 + wLen + 2];
                System.arraycopy(header, 0, message, 0, 16);
                System.arraycopy(packet, 0, message, 16, wLen+2);

                out.put(message);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}
