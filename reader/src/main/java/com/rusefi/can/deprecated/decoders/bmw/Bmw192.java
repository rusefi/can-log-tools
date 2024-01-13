package com.rusefi.can.deprecated.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.SensorValue;
import com.rusefi.can.deprecated.PacketPayload;
import com.rusefi.can.deprecated.SensorType;
import com.rusefi.can.deprecated.decoders.AbstractPacketDecoder;

/**
 * Gear change request
 */
public class Bmw192 extends AbstractPacketDecoder {
    public static final Bmw192 INSTANCE = new Bmw192();

    enum Value {
        NOTHING,
        NEUTRAL,
        REVERSE,
        DRIVE,
    }

    public static final int ID = 0x192;

    private Bmw192() {
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
        if (packet.getUnsignedInt(0) == 0x6A && packet.getUnsignedInt(1) == 0x40)
            return new PacketPayload(0, new SensorValue(SensorType.GEAR_CHANGE_REQUEST, Value.NOTHING.ordinal()));
        if (packet.getUnsignedInt(0) == 0x6A && packet.getUnsignedInt(1) == 0x50)
            return new PacketPayload(0, new SensorValue(SensorType.GEAR_CHANGE_REQUEST, Value.NOTHING.ordinal()));

        if (packet.getUnsignedInt(0) == 0x47 && packet.getUnsignedInt(1) == 1)
            return new PacketPayload(0, new SensorValue(SensorType.GEAR_CHANGE_REQUEST, Value.NEUTRAL.ordinal()));

        if (packet.getUnsignedInt(0) == 0x2D && packet.getUnsignedInt(1) == 4)
            return new PacketPayload(0, new SensorValue(SensorType.GEAR_CHANGE_REQUEST, Value.REVERSE.ordinal()));

        if (packet.getUnsignedInt(0) == 0x74 && packet.getUnsignedInt(1) == 3)
            return new PacketPayload(0, new SensorValue(SensorType.GEAR_CHANGE_REQUEST, Value.DRIVE.ordinal()));


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
