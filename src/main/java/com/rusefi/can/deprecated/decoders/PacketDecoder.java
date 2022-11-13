package com.rusefi.can.deprecated.decoders;

import com.rusefi.can.CANPacket;
import com.rusefi.can.deprecated.PacketPayload;

public interface PacketDecoder {
    PacketPayload decode(CANPacket packet);

    int getId();
}
