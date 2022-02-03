package com.rusefi.can.decoders;

import com.rusefi.can.CANPacket;
import com.rusefi.can.PacketPayload;

public interface PacketDecoder {
    PacketPayload decode(CANPacket packet);

    int getId();
}
