package com.rusefi.can.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.PacketPayload;
import com.rusefi.can.SensorType;
import com.rusefi.can.SensorValue;
import com.rusefi.can.decoders.AbstractPacketDecoder;

/**
 * Gear change request
 */
public class Bmw192 extends AbstractPacketDecoder {
    enum Value {
        NOTHING
    }

    public static final int ID = 0x192;

    public Bmw192() {
        super(ID);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        byte[] data = packet.getData();
        if (data.length != 4)
            throwUnexpected("length", packet);
        packet.assertThat("unexpected at at byte 2", p -> isA(p.getUnsignedInt(2), 0x80, 0xE0, 0xFF));
        packet.assertThat("xFx at at byte 3", p -> p.getUnsignedInt(3) >> 4 == 0xF);


        if (packet.getUnsignedInt(0) == 0x6A && packet.getUnsignedInt(1) == 0)
            return new PacketPayload(0, new SensorValue(SensorType.GEAR_CHANGE_REQUEST, Value.NOTHING.ordinal()));


        throwUnexpected("unhandled", packet);
        return null;
    }

    private boolean isA(int value, int... options) {
        for (int option : options) {
            if (value == option)
                return true;
        }
        return false;
    }
}
