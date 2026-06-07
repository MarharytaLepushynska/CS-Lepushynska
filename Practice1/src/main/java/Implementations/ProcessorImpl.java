package Implementations;

import Interfaces.Processor;
import Tools.Message;
import Tools.Storage;

import java.util.concurrent.ArrayBlockingQueue;

public class ProcessorImpl implements Processor, Runnable {
    private ArrayBlockingQueue<Message> queueDecr;
    private ArrayBlockingQueue<Message> queueEncr;
    private Storage storage;
    private volatile boolean stopped = false;

    public ProcessorImpl(ArrayBlockingQueue<Message> queueDecr, ArrayBlockingQueue<Message> queueEncr, Storage storage) {
        this.queueDecr = queueDecr;
        this.queueEncr = queueEncr;
        this.storage = storage;
    }

    @Override
    public void process(Message message) {
        String response = switch(message.getcType()) {
            case 1 -> {
                String product = message.getMessage().trim().toLowerCase();
                yield String.valueOf(storage.getProductCount(product));
            }
            case 2 -> {
                String[] partsR = message.getMessage().split(" ");
                String nameR = partsR[0].toLowerCase();
                int priceR = Integer.parseInt(partsR[1]);
                yield storage.reduceProductCount(nameR, priceR);
            }
            case 3 -> {
                String[] partsI = message.getMessage().split(" ");
                String nameI = partsI[0].toLowerCase();
                int countI = Integer.parseInt(partsI[1]);
                yield storage.increaseProductCount(nameI, countI);
            }
            case 4 -> {
                String category = message.getMessage().trim().toLowerCase();
                yield storage.addCategory(category);
            }
            case 5 -> {
                String[] partsC = message.getMessage().split(" ");
                String nameCategory = partsC[0].toLowerCase();
                String nameProduct = partsC[1].toLowerCase();
                yield storage.addProduct(nameCategory, nameProduct);
            }
            case 6 -> {
                String[] partsP = message.getMessage().split(" ");
                String nameP = partsP[0].toLowerCase();
                double priceP = Double.parseDouble(partsP[1]);
                yield storage.addProductPrice(nameP, priceP);
            }
            default -> "Unknown cType: " + message.getcType();
        };
        System.out.println(response);

        Message responseMessage = new Message(
                message.getbSrc(),
                message.getbPktId(),
                message.getcType(),
                message.getbUserId(),
                "OK"
        );
        try {
            queueEncr.put(responseMessage);
        } catch (InterruptedException e) {
            throw new RuntimeException(e + "Thread was interrupted");
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
}
