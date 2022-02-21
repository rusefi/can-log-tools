package com.rusefi.can.decoders;

import com.rusefi.can.CANPacket;

import java.util.Arrays;

import static com.rusefi.can.Utils.bytesToHexWithSpaces;

public abstract class AbstractPacketDecoder implements PacketDecoder {
    private final int id;

    public AbstractPacketDecoder(int id) {
        this.id = id;
    }

    public static int mask(int bitWidth) {
        return (1 << bitWidth) - 1;
    }

    @Override
    public int getId() {
        return id;
    }

    protected void throwUnexpected(String reason, CANPacket packet) {
        throw unexpected(reason, packet);
    }

    protected IllegalStateException unexpected(String reason, CANPacket packet) {
        return new IllegalStateException(reason + ": " + bytesToHexWithSpaces(packet.getData()));
    }
}
