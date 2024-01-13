package com.rusefi.io.can;

import org.jetbrains.annotations.NotNull;
import tel.schich.javacan.CanChannels;
import tel.schich.javacan.CanFrame;
import tel.schich.javacan.NetworkDevice;
import tel.schich.javacan.RawCanChannel;

import java.io.IOException;

import static tel.schich.javacan.CanFrame.FD_NO_FLAGS;
import static tel.schich.javacan.CanSocketOptions.RECV_OWN_MSGS;

public class SocketCANHelper {
    @NotNull
    public static RawCanChannel createSocket() {
        final RawCanChannel socket;
        try {
            NetworkDevice canInterface = NetworkDevice.lookup(System.getProperty("CAN_DEVICE_NAME", "can0"));
            socket = CanChannels.newRawChannel();
            socket.bind(canInterface);

            socket.configureBlocking(true); // we want reader thread to wait for messages
            socket.setOption(RECV_OWN_MSGS, false);
        } catch (IOException e) {
            throw new IllegalStateException("Error looking up", e);
        }
        return socket;
    }

    public static void send(int id, byte[] payload, RawCanChannel channel) throws IOException {
        CanFrame packet = CanFrame.create(id, FD_NO_FLAGS, payload);
        channel.write(packet);
    }

    public static CanSender create() {
        RawCanChannel canChannel = createSocket();
        return new CanSender() {
            @Override
            public boolean send(int id, byte[] payload) {
                try {
                    SocketCANHelper.send(id, payload, canChannel);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        };
    }
}
