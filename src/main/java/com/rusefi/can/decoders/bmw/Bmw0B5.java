package com.rusefi.can.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.PacketPayload;
import com.rusefi.can.decoders.AbstractPacketDecoder;

public class Bmw0B5 extends AbstractPacketDecoder {
    public static final int ID = 0xBA;

    public Bmw0B5() {
        super(ID);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        return null;
    }
}
