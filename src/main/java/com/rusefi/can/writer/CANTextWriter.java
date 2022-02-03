package com.rusefi.can.writer;

import com.rusefi.can.CANPacket;

import java.io.IOException;
import java.util.List;

public interface CANTextWriter {
    void write(List<CANPacket> packetList) throws IOException;
}
