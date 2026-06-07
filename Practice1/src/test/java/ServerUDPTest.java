import Network.ServerUDP;
import Network.StoreClientUDP;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ServerUDPTest {

    @Test
    void testClientAddsProducts() throws IOException, InterruptedException {
        ServerUDP server = new ServerUDP();
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> clients = new ArrayBlockingQueue<>(50);
        new Thread(new StoreClientUDP(clients)).start();
        new Thread(new StoreClientUDP(clients)).start();
        Thread.sleep(1000);

        for(int i = 0; i < 10; i++) {
            clients.put("3 квас 8");
        }

        Thread.sleep(3000);
        assertEquals(80, server.getStorage().getProductCount("квас"));
        server.stop();
    }
}
