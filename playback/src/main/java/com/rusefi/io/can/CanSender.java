package com.rusefi.io.can;

public interface CanSender {
    void send(int id, byte[] payload);
}
