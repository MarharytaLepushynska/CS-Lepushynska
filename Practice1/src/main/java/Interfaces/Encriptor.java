package Interfaces;

import Tools.Message;

public interface Encriptor {
    void encrypt(Message message);

    void stop();
}
