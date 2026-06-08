package Implementations;

import Db.StorageService;
import Interfaces.Processor;
import Tools.Message;
import Tools.Product;
import Tools.ProductFilter;
import Tools.ProductUpdate;

import java.util.concurrent.ArrayBlockingQueue;

public class ProcessorDb implements Processor, Runnable {
    private ArrayBlockingQueue<Message> queueDecr;
    private ArrayBlockingQueue<Message> queueEncr;
    private volatile boolean stopped = false;
    private final StorageService storageService;

    public ProcessorDb(ArrayBlockingQueue<Message> queueDecr, ArrayBlockingQueue<Message> queueEncr, StorageService storageService) {
        this.queueDecr = queueDecr;
        this.queueEncr = queueEncr;
        this.storageService = storageService;
    }

    @Override
    public void process(Message message) {
        synchronized (storageService) {
            String text = message.getMessage().trim().toLowerCase();

            String response = switch (message.getcType()) {
                case 1 -> storageService.getById(Integer.parseInt(text));
                case 2 -> storageService.create(parseProduct(text));
                case 3 -> storageService.update(parseUpdate(text));
                case 4 -> storageService.deleteById(Integer.parseInt(text));
                case 5 -> storageService.deleteAll();
                case 6 -> storageService.findAndFilter(parseFilter(text));
                default -> "Unknown command";
            };

            System.out.println(response);

            Message responseMessage = new Message(
                    message.getbSrc(),
                    message.getbPktId(),
                    message.getcType(),
                    message.getbUserId(),
                    response
            );

            try {
                queueEncr.put(responseMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException("Problem while processing", e);
            }
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Message message = queueDecr.take();
                process(message);
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e + "Processing failed");
            }
        }
    }

    public void stop() {
        stopped = true;
    }

    private Product parseProduct(String product) {
        String[] parts = product.split(" ");
        return new Product(parts[0], Integer.parseInt(parts[1]), Double.parseDouble(parts[2]), parts[3]);
    }

    private ProductFilter parseFilter(String filter) {
        ProductFilter.ProductFilterBuilder pb = ProductFilter.builder();

        for (String s : filter.split(" ")) {
            String[] colVal = s.split("=");
            switch (colVal[0]) {
                case "name" -> pb.name(colVal[1]);
                case "amount_from" -> pb.amountFrom(Integer.parseInt(colVal[1]));
                case "amount_to" -> pb.amountTo(Integer.parseInt(colVal[1]));
                case "price_from" -> pb.priceFrom(Double.parseDouble(colVal[1]));
                case "price_to" -> pb.priceTo(Double.parseDouble(colVal[1]));
                case "category" -> pb.category(colVal[1]);
                case "page_number" -> pb.pageNumber(Integer.parseInt(colVal[1]));
                case "page_size" -> pb.pageSize(Integer.parseInt(colVal[1]));
            }
        }

        return pb.build();
    }

    private ProductUpdate parseUpdate(String update) {
        ProductUpdate.ProductUpdateBuilder pb = ProductUpdate.builder();

        for (String s : update.split(" ")) {
            String[] colVal = s.split("=");
            switch (colVal[0]) {
                case "id" -> pb.id(Integer.parseInt(colVal[1]));
                case "name" -> pb.name(colVal[1]);
                case "amount" -> pb.amount(Integer.parseInt(colVal[1]));
                case "price" -> pb.price(Double.parseDouble(colVal[1]));
                case "category" -> pb.category(colVal[1]);
            }
        }

        return pb.build();
    }
}
