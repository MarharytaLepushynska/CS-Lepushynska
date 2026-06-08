import Db.StorageService;
import Network.ServerTCP;
import Network.StoreClientTCP;
import Tools.Decoder;
import Tools.Message;
import Tools.Product;
import Tools.Storage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ServerTCPTest {
    private static StorageService service = new StorageService();
    private Decoder decoder = new Decoder();

    @Test
    void shouldCreateProduct() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        new Thread(new StoreClientTCP(queue)).start();
        new Thread(new StoreClientTCP(queue)).start();
        Thread.sleep(1000);


        queue.put("2 рис 5 70 крупи");

        Thread.sleep(4000);
        assertEquals(5, service.getAmountByName("рис"));
        server.stop();
    }

    @Test
    void shouldGetProductById() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("монстр");

        queue.put("1 " + id);

        Thread.sleep(4000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("монстр"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        server.stop();
    }

    @Test
    void shouldUpdateProduct() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("баклажан");


        queue.put("3 id=" + id + " name=булгур category=крупи");

        Thread.sleep(4000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("булгур"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        server.stop();
    }

    @Test
    void shouldDeleteProduct() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("огірок");

        queue.put("4 " + id);

        queue.put("1 " + id);


        Thread.sleep(4000);

        client.getResponses().poll();
        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("Product not found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        server.stop();
    }

    @Test
    void deleteAllProducts() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);


        queue.put("5");

        queue.put("6");


        Thread.sleep(4000);

        client.getResponses().poll();
        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("No products found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        server.stop();
    }

    @Test
    void shouldFilterProducts() throws InterruptedException{
        ServerTCP server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        queue.put("6 price_from=12 price_to=30 category=солодке");


        Thread.sleep(4000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertFalse(message.getMessage().contains("желе"));
            assertTrue(message.getMessage().contains("бджілка"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        server.stop();
    }

    @BeforeAll
    static void setUp() {
        Product p1 = new Product("монстр", 10, 24.5, "напої");
        Product p2 = new Product("баклажан", 20, 4.5, "овочі");
        Product p3 = new Product("огірок", 20, 4.5, "овочі");
        Product p4 = new Product("печиво", 10, 9.0, "солодке");
        Product p5 = new Product("желе", 15, 10.0, "солодке");
        Product p6 = new Product("бджілка", 20, 14.5, "солодке");

        service.create(p1);
        service.create(p2);
        service.create(p3);
        service.create(p4);
        service.create(p5);
        service.create(p6);
    }

    @AfterAll
    static void cleanUp() {
        service.deleteAll();
    }
}
