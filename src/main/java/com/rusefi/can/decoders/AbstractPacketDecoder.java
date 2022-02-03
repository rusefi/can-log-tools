package com.rusefi.can.decoders;

public abstract class AbstractPacketDecoder implements PacketDecoder {
    private final int id;

    public AbstractPacketDecoder(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
