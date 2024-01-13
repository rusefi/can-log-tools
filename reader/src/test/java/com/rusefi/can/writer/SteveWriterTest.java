package com.rusefi.can.writer;

import com.rusefi.can.CANPacket;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SteveWriterTest {
    @Test
    public void testWriteLine() throws IOException {
        CANPacket testPacket = new CANPacket(90.229, 0x0b4, new byte[] {0, (byte) 0xbc});

        StringBuilder sb = new StringBuilder();

        SteveWriter.append(sb, testPacket);

        assertEquals("90.229:\t0b4\t2\t00\tbc\n", sb.toString());


    }
}
