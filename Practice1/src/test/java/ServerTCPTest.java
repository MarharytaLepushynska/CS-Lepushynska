import Network.ServerTCP;
import Network.StoreClientTCP;
import Tools.Storage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ServerTCPTest {

    @Test
    void testClientAddsProduct() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        new Thread(new StoreClientTCP(queue)).start();
        new Thread(new StoreClientTCP(queue)).start();
        Thread.sleep(1000);

        for(int i = 0; i < 10; i++){
            queue.put("3 гречка 5");
        }

        Thread.sleep(4000);
        assertEquals(50, server.getStorage().getProductCount("гречка"));
        server.stop();
    }

}
