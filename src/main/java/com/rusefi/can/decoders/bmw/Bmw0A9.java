package com.rusefi.can.decoders.bmw;

import com.rusefi.can.CANPacket;
import com.rusefi.can.PacketPayload;
import com.rusefi.can.SensorType;
import com.rusefi.can.SensorValue;
import com.rusefi.can.decoders.AbstractPacketDecoder;

public class Bmw0A9 extends AbstractPacketDecoder {
    public static final AbstractPacketDecoder INSTANCE = new Bmw0A9();

    public static final int ID = 0xA9;

    public Bmw0A9() {
        super(ID);
    }

    @Override
    public PacketPayload decode(CANPacket packet) {
        int TORQ_AVL_MAX = (int) (packet.getByBitIndex(28, 12) * 0.5);
        return new PacketPayload(packet.getTimeStamp(),
                new SensorValue(SensorType.TORQ_AVL_MAX, TORQ_AVL_MAX)
        );
    }
}
