package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class EcuMasterDongleReaderTest {
    @Test
    public void test() {
        String line = "    13:        20.0  Rx          090  8  22 FE 19 09 07 00 2B 00";
        CANLineReader reader = EcuMasterDongleReader.INSTANCE;
        CANPacket packet = reader.readLine(line);

        assertEquals(0x090, packet.getId());
        assertEquals(8, packet.getData().length);

    }
}
