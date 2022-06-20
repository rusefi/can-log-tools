package com.rusefi.can.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.PacketPayload;
import com.rusefi.can.SensorType;
import com.rusefi.can.SensorValue;
import com.rusefi.can.decoders.AbstractPacketDecoder;

public class Bmw0B5 extends AbstractPacketDecoder {
    public static final AbstractPacketDecoder INSTANCE = new Bmw0B5();

    public Bmw0B5() {
        super(0xB5);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        int TORQ_TAR_EGS = (int) (packet.getByBitIndex(12, 12) * 0.5);
        int TORQ_TAR_ADJR_POS_EGS = packet.getByBitIndex(24, 12);
        int ST_TORQ_TAR_EGS = packet.getByBitIndex(36, 2);
        return new PacketPayload(packet.getTimeStamp(),
                new SensorValue(SensorType.GEARBOX_CURRENT_TORQUE, TORQ_TAR_EGS),
                new SensorValue(SensorType.GEARBOX_TORQUE_CHANGE_REQUEST, ST_TORQ_TAR_EGS));
    }
}
