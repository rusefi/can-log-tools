package com.rusefi.can.reader;

import com.rusefi.can.CANPacket;

public interface CANLineReader {
    CANPacket readLine(String line);
}
