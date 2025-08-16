package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IxxatReaderTest {
    @Test
    public void readLine() {
        CANLineReader reader = IxxatReader.INSTANCE;
//        reader.readLine("Start Time  Sunday June 15 2025 11:20:17");
        {
            CANPacket p = reader.readLine("\"USB-to-CAN V2 compact  CAN-1\",\"6\",\"00:00:00.085\",\"      \",\"199\",\"8\",\"4F FF 0F FF F0 00 00 FF\",\"O.......\"");
            assertEquals(0x199, p.getId());
            assertEquals(8, p.getData().length);
        }

        {
            CANPacket p = reader.readLine("\"USB-to-CAN V2 compact  CAN-1\",\"1,000\",\"00:00:00.774\",\"      \",\"1EF\",\"4\",\"80 00 00 00\",\"....\"");
            assertEquals(0x1EF, p.getId());
            assertEquals(4, p.getData().length);
        }

        AutoFormatReader.INSTANCE.detectReader("Start Time  Sunday June 15 2025 11:20:17");
        AutoFormatReader.INSTANCE.detectReader("\"Bus\",\"No\",\"Time (abs)\",\"State\",\"ID (hex)\",\"DLC\",\"Data (hex)\",\"ASCII\"");
    }
}
