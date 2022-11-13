package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.deprecated.decoders.bmw.Bmw192;
import com.rusefi.can.reader.CANLineReader;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PcanReaderTest {
    @Test
    public void testLine() {
        CANLineReader reader = PcanTrcReader2_0.INSTANCE;
        CANPacket packet = reader.readLine("  15883  77333097.212 DT     0192 Rx 4  2D 04 80 F9 ");
        assertEquals(4, packet.getData().length);
        assertEquals(Bmw192.ID, packet.getId());

        assertEquals(0x80, packet.getUnsigned(2));
    }
}
