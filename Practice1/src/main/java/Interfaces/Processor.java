package Interfaces;

import Tools.Message;

public interface Processor {
    void process(Message message);

    void stop();
}
