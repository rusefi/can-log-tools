package com.rusefi.can.deprecated.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.deprecated.PacketPayload;
import com.rusefi.can.deprecated.decoders.AbstractPacketDecoder;

public class Bmw0B6 extends AbstractPacketDecoder {
    public static final AbstractPacketDecoder INSTANCE = new Bmw0B6();

    public Bmw0B6() {
        super(0xB6);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        return null;
    }
}
