package com.rusefi.can.deprecated.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.deprecated.PacketPayload;
import com.rusefi.can.deprecated.SensorType;
import com.rusefi.can.SensorValue;
import com.rusefi.can.deprecated.decoders.AbstractPacketDecoder;

public class Bmw0AA extends AbstractPacketDecoder {
    public static final int ID = 0xAA;

    public Bmw0AA() {
        super(ID);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        SensorValue pedal = new SensorValue(SensorType.PPS, packet.getUnsigned(3) * 0.39063);
        int rawRpm = packet.getTwoBytesByByteIndex(4);
        if (rawRpm == 0xFFFF)
            return null;
        SensorValue rpm = new SensorValue(SensorType.RPM, rawRpm * 0.25);
        return new PacketPayload(packet.getTimeStamp(), pedal, rpm);
    }

}
