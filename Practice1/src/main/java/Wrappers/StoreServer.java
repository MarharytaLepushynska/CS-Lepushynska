package Wrappers;

import Implementations.DecriptorImpl;
import Implementations.EncriptorImpl;
import Implementations.ProcessorImpl;
import Interfaces.*;
import Tools.Message;
import Tools.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreServer {
    private final ArrayBlockingQueue<byte[]> toDecriptor;
    private final ArrayBlockingQueue<Message> toProcessor;
    private final ArrayBlockingQueue<Message> toEncriptor;
    private final ArrayBlockingQueue<byte[]> toSender;

    private final Storage storage;

    private final List<Receiver> receiversList;
    private final List<Decriptor> decriptorsList;
    private final List<Processor> prosessorsList;
    private final List<Encriptor> encriptorsList;
    private final List<Sender> sendersList;

    private ExecutorService receivers;
    private ExecutorService decriptors;
    private ExecutorService processors;
    private ExecutorService encriptors;
    private ExecutorService senders;

    private int decCount;
    private int prosCount;
    private int encCount;

    public StoreServer(Storage storage,
                       ArrayBlockingQueue<byte[]> toDecriptor,
                       ArrayBlockingQueue<Message> toProcessor,
                        ArrayBlockingQueue<Message> toEncriptor,
                        ArrayBlockingQueue<byte[]> toSender,
                        List<Receiver> receivers,
                        List<Sender> senders, int decCount, int prosCount, int encCount) {
        this.storage = storage;
        this.toDecriptor = toDecriptor;
        this.toProcessor = toProcessor;
        this.toEncriptor = toEncriptor;
        this.toSender = toSender;

        this.receiversList = receivers;
        this.sendersList = senders;
        decriptorsList = new ArrayList<>();
        prosessorsList = new ArrayList<>();
        encriptorsList = new ArrayList<>();

        this.receivers = Executors.newCachedThreadPool();
        this.decriptors = Executors.newFixedThreadPool(decCount);
        this.processors = Executors.newFixedThreadPool(prosCount);
        this.encriptors = Executors.newFixedThreadPool(encCount);
        this.senders = Executors.newCachedThreadPool();

        this.decCount = decCount;
        this.prosCount = prosCount;
        this.encCount = encCount;
    }

    public void start() {
        for(Receiver receiver : receiversList) {
            receivers.submit((Runnable) receiver);
        }

        for(int i = 0; i < decCount; i++) {
            DecriptorImpl decriptor = new DecriptorImpl(toDecriptor, toProcessor);
            decriptorsList.add(decriptor);
            decriptors.submit(decriptor);
        }

        for (int i = 0; i < prosCount; i++) {
            ProcessorImpl processor = new ProcessorImpl(toProcessor, toEncriptor, storage);
            prosessorsList.add(processor);
            processors.submit(processor);
        }

        for(int i = 0; i < encCount; i++) {
            EncriptorImpl encriptor = new EncriptorImpl(toEncriptor, toSender);
            encriptorsList.add(encriptor);
            encriptors.submit(encriptor);
        }

        for(Sender sender : sendersList) {
            senders.submit((Runnable) sender);
        }
    }

    public void stop() {
        receiversList.forEach(Receiver::stop);
        receivers.shutdown();

        decriptorsList.forEach(Decriptor::stop);
        decriptors.shutdown();

        prosessorsList.forEach(Processor::stop);
        processors.shutdown();

        encriptorsList.forEach(Encriptor::stop);
        encriptors.shutdown();

        sendersList.forEach(Sender::stop);
        senders.shutdown();
    }

    public ArrayBlockingQueue<byte[]> getToDecriptor() {
        return toDecriptor;
    }

    public ArrayBlockingQueue<byte[]> getToSender() {
        return toSender;
    }
}
