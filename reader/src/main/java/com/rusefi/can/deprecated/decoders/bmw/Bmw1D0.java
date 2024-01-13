package com.rusefi.can.deprecated.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.SensorValue;
import com.rusefi.can.deprecated.PacketPayload;
import com.rusefi.can.deprecated.SensorType;
import com.rusefi.can.deprecated.decoders.AbstractPacketDecoder;

public class Bmw1D0 extends AbstractPacketDecoder {
    public Bmw1D0() {
        super(0x1d0);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        SensorValue clt = new SensorValue(SensorType.CLT, packet.getUnsigned(0) - 48);
        SensorValue map = new SensorValue(SensorType.MAP, packet.getUnsigned(3) * 0.2 + 59.8);
        SensorValue fuel = new SensorValue(SensorType.FUEL_AMOUNT, packet.getTwoBytesByByteIndex(4));
        return new PacketPayload(packet.getTimeStamp(), clt, map, fuel);
    }
}
