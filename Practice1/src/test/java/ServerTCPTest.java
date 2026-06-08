import Db.StorageDb;
import Db.StorageService;
import Network.ServerTCP;
import Network.StoreClientTCP;
import Tools.Decoder;
import Tools.Message;
import Tools.Product;
import Tools.Storage;
import org.junit.jupiter.api.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ServerTCPTest {
    private StorageService service = new StorageService();
    private StorageDb db = new StorageDb("jdbc:mysql://localhost:3306/store_db", "root", "root");
    private Decoder decoder = new Decoder();
    private ServerTCP server;

    @Test
    void shouldCreateProduct() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        ArrayBlockingQueue<String> queue2 = new ArrayBlockingQueue<>(50);
        new Thread(new StoreClientTCP(queue)).start();
        new Thread(new StoreClientTCP(queue2)).start();
        Thread.sleep(1000);

        queue.put("2 рис 5 70 крупи");
        queue2.put("2 сочевиця 40 50 крупи");

        Thread.sleep(1000);
        assertEquals(5, service.getAmountByName("рис"));

        int id1 = service.getIdByName("рис");
        Product p1 = db.getById(id1);
        assertEquals("рис", p1.getName());
        assertEquals(5, p1.getAmount());
        assertEquals(70.0, p1.getPrice());
        assertEquals("крупи", p1.getCategory());

        int id2 = service.getIdByName("сочевиця");
        Product p2 = db.getById(id2);
        assertEquals("сочевиця", p2.getName());
        assertEquals(40, p2.getAmount());
        assertEquals(50.0, p2.getPrice());
        assertEquals("крупи", p2.getCategory());
    }

    @Test
    void shouldGetProductById() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("монстр");

        queue.put("1 " + id);

        Thread.sleep(1000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("монстр"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldUpdateProduct() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("баклажан");


        queue.put("3 id=" + id + " name=булгур category=крупи");

        Thread.sleep(1000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("булгур"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldDeleteProduct() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("огірок");

        queue.put("4 " + id);

        queue.put("1 " + id);


        Thread.sleep(1000);

        client.getResponses().poll();
        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("Product not found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteAllProducts() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        queue.put("5");
        queue.put("6");

        Thread.sleep(1000);

        client.getResponses().take();
        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertTrue(message.getMessage().contains("No products found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldFilterProducts() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        queue.put("6 price_from=12 price_to=30 category=солодке");

        Thread.sleep(1000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            assertFalse(message.getMessage().contains("желе"));
            assertTrue(message.getMessage().contains("бджілка"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldAddPagination() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        queue.put("6 page_number=2 page_size=3");


        Thread.sleep(1000);

        byte[] res = client.getResponses().take();
        try {
            Message message = decoder.decode(res);
            String text = message.getMessage();

            long count = Stream.of(text.split("\n"))
                    .filter(line -> !line.isEmpty())
                    .count();

            assertTrue(count <= 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldProcessMultipleCommands() throws InterruptedException{
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        StoreClientTCP client = new StoreClientTCP(queue);
        new Thread(client).start();

        Thread.sleep(1000);

        int id = service.getIdByName("баклажан");

        queue.put("3 id=" + id + " name=булгур category=крупи");
        queue.put("2 макарони 100 50 крупи");
        queue.put("3 id=" + id + " name=мак price=10 category=крупи");

        Thread.sleep(1000);

        Product p1 = db.getById(id);
        assertEquals("мак", p1.getName());
        assertEquals(10, (double) p1.getPrice());
        assertEquals("крупи", p1.getCategory());

        int id2 = service.getIdByName("макарони");
        Product p2 = db.getById(id2);
        assertEquals(p2.getName(), "макарони");
        assertEquals(p2.getAmount(), 100);
        assertEquals(p2.getPrice(), 50.0);
        assertEquals(p2.getCategory(), "крупи");
    }

    @BeforeEach
    void startServer() throws InterruptedException {
        server = new ServerTCP(1, 1);
        server.start();
        Thread.sleep(1000);
    }

    @AfterEach
    void stopServer(){
        server.stop();
    }

    @BeforeEach
    void setUp() {
        ArrayList<Product> products = new ArrayList<>();
        products.add(new Product("монстр", 10, 24.5, "напої"));
        products.add(new Product("баклажан", 20, 4.5, "овочі"));
        products.add(new Product("огірок", 20, 4.5, "овочі"));
        products.add(new Product("печиво", 10, 9.0, "солодке"));
        products.add(new Product("желе", 15, 10.0, "солодке"));
        products.add(new Product("бджілка", 20, 14.5, "солодке"));
        products.add(new Product("зефір", 30, 50.99, "солодке"));

        for(Product product: products) {
            service.create(product);
        }
    }

    @AfterEach
    void cleanUp() {
        service.deleteAll();
    }
}
