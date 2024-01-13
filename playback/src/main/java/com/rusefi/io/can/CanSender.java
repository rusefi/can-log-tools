package com.rusefi.io.can;

public interface CanSender {
    boolean send(int id, byte[] payload);
}
