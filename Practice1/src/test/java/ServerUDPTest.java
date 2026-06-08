import Db.StorageService;
import Network.ServerUDP;
import Network.StoreClientUDP;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ServerUDPTest {
    private StorageService service = new StorageService();

    @Test
    void testClientAddsProducts() throws IOException, InterruptedException {
        ServerUDP server = new ServerUDP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> clients = new ArrayBlockingQueue<>(50);
        new Thread(new StoreClientUDP(clients)).start();
        new Thread(new StoreClientUDP(clients)).start();
        Thread.sleep(1000);


        clients.put("2 манка 8 5 крупи");


        Thread.sleep(3000);
        assertEquals(8, service.getAmountByName("манка"));
        server.stop();
    }
}
