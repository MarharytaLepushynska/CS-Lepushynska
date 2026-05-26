import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ArrayBlockingQueue<String> inQueueTest;
    private ArrayBlockingQueue<byte[]> toDecriptor = new ArrayBlockingQueue<>(50);
    private ArrayBlockingQueue<Message> toProcessor = new ArrayBlockingQueue<>(50);
    private ArrayBlockingQueue<Message> toEncriptor = new ArrayBlockingQueue<>(50);
    private ArrayBlockingQueue<byte[]> toSender = new ArrayBlockingQueue<>(50);

    private final int receiversCount;
    private final int decriptorsCount;
    private final int processorsCount;
    private final int encriptorsCount;
    private final int sendersCount;


    private ExecutorService receivers;
    private ExecutorService decriptors;
    private ExecutorService processors;
    private ExecutorService encriptors;
    private ExecutorService senders;

    private ArrayList<ReceiverImpl> receiversList = new ArrayList<>();
    private ArrayList<DecriptorImpl> decriptorsList = new ArrayList<>();
    private ArrayList<ProcessorImpl> processorsList  = new ArrayList<>();
    private ArrayList<EncriptorImpl> encriptorsList  = new ArrayList<>();
    private ArrayList<SenderImpl> sendersList  = new ArrayList<>();

    Storage storage;


    public Server(Storage storage, ArrayBlockingQueue<String> inQueueTest, int receiversCount, int decriptorsCount, int processorsCount, int encriptorsCount, int sendersCount) {
        this.inQueueTest = inQueueTest;
        this.storage = storage;

        this.receiversCount = receiversCount;
        this.decriptorsCount = decriptorsCount;
        this.processorsCount = processorsCount;
        this.encriptorsCount = encriptorsCount;
        this.sendersCount = sendersCount;

        receivers = Executors.newFixedThreadPool(receiversCount);
        decriptors = Executors.newFixedThreadPool(decriptorsCount);
        processors = Executors.newFixedThreadPool(processorsCount);
        encriptors = Executors.newFixedThreadPool(encriptorsCount);
        senders = Executors.newFixedThreadPool(sendersCount);
    }

    public void start() {
        for (int i = 0; i < receiversCount; i++) {
            ReceiverImpl receiver = new ReceiverImpl(inQueueTest, toDecriptor);
            receiversList.add(receiver);
            receivers.submit(receiver);
        }

        for(int i = 0; i < decriptorsCount; i++) {
            DecriptorImpl decriptor = new DecriptorImpl(toDecriptor, toProcessor);
            decriptorsList.add(decriptor);
            decriptors.submit(decriptor);
        }

        for (int i = 0; i < processorsCount; i++) {
            ProcessorImpl processor = new ProcessorImpl(toProcessor, toEncriptor, storage);
            processorsList.add(processor);
            processors.submit(processor);
        }

        for(int i = 0; i < encriptorsCount; i++) {
            EncriptorImpl encriptor = new EncriptorImpl(toEncriptor, toSender);
            encriptorsList.add(encriptor);
            encriptors.submit(encriptor);
        }

        for(int i = 0; i < sendersCount; i++) {
            SenderImpl sender = new SenderImpl(toSender);
            sendersList.add(sender);
            senders.submit(sender);
        }
    }

    public void stop() {
        receiversList.forEach(ReceiverImpl::stop);
        receivers.shutdown();

        decriptorsList.forEach(DecriptorImpl::stop);
        decriptors.shutdown();

        processorsList.forEach(ProcessorImpl::stop);
        processors.shutdown();

        encriptorsList.forEach(EncriptorImpl::stop);
        encriptors.shutdown();

        sendersList.forEach(SenderImpl::stop);
        senders.shutdown();
    }

    public Storage getStorage() {
        return storage;
    }
}
