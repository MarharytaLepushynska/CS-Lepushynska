public interface Processor {
    void process(Message message);

    void stop();
}
