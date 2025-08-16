package com.rusefi.can.reader.impl;

import com.rusefi.can.reader.CANLineReader;
import org.junit.Test;

public class TodoReaderTest {
    @Test
    public void readLine() {
        CANLineReader reader = TodoReader.INSTANCE;
//        reader.readLine("Start Time  Sunday June 15 2025 11:20:17");
        reader.readLine("\"USB-to-CAN V2 compact  CAN-1\",\"6\",\"00:00:00.085\",\"      \",\"199\",\"8\",\"4F FF 0F FF F0 00 00 FF\",\"O.......\"");

    }
}
