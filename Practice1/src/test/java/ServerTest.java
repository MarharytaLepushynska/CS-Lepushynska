import Tools.Storage;
import Wrappers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    private Storage storage;

    @Test
    void shouldAddProductAndCheckCount() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    queue.put("3 квас 5");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Server server = new Server(storage, queue, 1, 1, 1, 1, 1);
        server.start();

        Thread.sleep(1500);
        assertEquals(50, storage.getProductCount("квас"));
        server.stop();
    }

    @Test
    void shouldRemoveProductAndCheckCount() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        for (int i = 0; i < 6; i++) {
            new Thread(() -> {
                try {
                    queue.put("2 гречка 5");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Server server = new Server(storage, queue, 1, 1, 1, 1, 1);
        server.start();

        Thread.sleep(1500);
        assertEquals(20, storage.getProductCount("гречка"));
        server.stop();
    }

    @Test
    void shouldAddCategory() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        Thread t1 = new Thread(() -> {
            try {
                queue.put("4 молочка");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                queue.put("4 напої");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                queue.put("4 крупи");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();
        t3.start();

        Server server = new Server(storage, queue, 1, 1, 1, 1, 1);
        server.start();

        Thread.sleep(1000);
        assertTrue(storage.isCategoryPresent("молочка"));
        assertTrue(storage.isCategoryPresent("напої"));
        assertTrue(storage.isCategoryPresent("крупи"));
        server.stop();
    }

    @Test
    void shouldAddProductToCategory() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        Thread t1 = new Thread(() -> {
            try {
                queue.put("5 крупи гречка");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                queue.put("5 крупи рис");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                queue.put("5 напої квас");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t1.start();
        t2.start();
        t3.start();

        Server server = new Server(storage, queue, 1, 1, 1, 1, 1);
        server.start();
        Thread.sleep(1000);
        assertTrue(storage.isProductPresentInCategory("крупи", "гречка"));
        assertTrue(storage.isProductPresentInCategory("крупи", "рис"));
        assertTrue(storage.isProductPresentInCategory("напої", "квас"));
        server.stop();
    }

    @Test
    void shouldAddPriceToProduct() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
        Thread t1 = new Thread(() -> {
            try {
                queue.put("6 гречка 100");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                queue.put("6 рис 120");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();
        Server server = new Server(storage, queue, 1, 1, 1, 1, 1);
        server.start();
        Thread.sleep(1000);

        assertTrue(storage.isPricePresentInProduct("гречка", 100));
        assertTrue(storage.isPricePresentInProduct("рис", 120));
        server.stop();
    }

    @Test
    void shouldIncreaseAndReduceProductCount() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    queue.put("3 гречка 5");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    queue.put("2 рис 5");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    queue.put("2 гречка 10");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    queue.put("3 рис 10");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Server server = new Server(storage, queue, 1, 1, 1, 1, 1);
        server.start();

        Thread.sleep(4000);
        assertEquals(45, storage.getProductCount("гречка"));
        assertEquals(55, storage.getProductCount("рис"));
        server.stop();
    }

    @Test
    void shouldWOrdWithMoneThanOneOfEachElement() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        for (int i = 0; i < 18; i++) {
            new Thread(() -> {
                try {
                    queue.put("3 квас 10");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Server server = new Server(storage, queue, 2, 3, 4, 2, 3);
        server.start();

        Thread.sleep(1500);
        assertEquals(180, storage.getProductCount("квас"));
        server.stop();
    }

    @BeforeEach
    void addProductsForTests() {
        storage = new Storage();
        storage.addTestProduct("гречка", 50);
        storage.addTestProduct("рис", 30);
    }
}
