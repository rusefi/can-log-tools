package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class CanHackerTest {
    private static final String CONTENT = "@ TEXT @ 3 @ 64 @ 0 @ 38624 @ 21032 @ 00:00:21.032 @\n" +
            "17,439466\t1\t0004\t4A0\t8\tCC FF 00 00 00 00 CC FF\t00000000\t        \t\n" +
            "17,439881\t1\t0004\t448\t5\t82 00 D4 13 70         \t00000000\t    p   \t\n" +
            "17,445456\t1\t0004\t280\t8\t03 00 00 00 00 FF 38 00\t00000000\t      8 \t\n" +
            "17,445705\t1\t0004\t380\t8\t12 0B FF 00 00 00 30 00\t00000000\t      0 \t\n" +
            "17,445947\t1\t0004\t440\t8\t00 80 00 FE 7F 00 B8 04\t00000000\t        \t\n" +
            "17,446191\t1\t0004\t488\t8\t2D 00 00 80 FD FF FF 50\t00000000\t-      P\t\n" +
            "17,446433\t1\t0004\t540\t8\t70 00 FF 00 FF 00 00 26\t00000000\tp      &\t\n" +
            "17,446625\t1\t0004\t548\t3\t81 00 00               \t00000000\t        \t\n" +
            "17,447022\t1\t0004\t284\t6\t0C 0C 00 00 00 00      \t00000000\t        \t\n" +
            "17,447262\t1\t0004\t288\t8\tE8 FF C7 04 00 78 5A 00\t00000000\t     xZ \t\n" +
            "17,447500\t1\t0004\t480\t8\tC2 24 00 00 FF 09 10 00\t00000000\t $      \t\n" +
            "17,447749\t1\t0004\t588\t8\tF8 00 80 00 00 10 00 00\t00000000\t        \t\n" +
            "17,447991\t1\t0004\t48A\t8\tC2 00 00 02 00 00 00 C0\t00000000\t        \t\n" +
            "17,448555\t1\t0004\t572\t1\t07                     \t00000000\t        \t\n" +
            "17,448797\t1\t0004\t0AE\t8\t91 90 00 00 00 00 01 00\t00000000\t        \t\n" +
            "17,449039\t1\t0004\t390\t8\t03 00 18 80 00 00 30 08\t00000000\t      0 \t";

    @Test
    public void testRead() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(CONTENT));
        assertNull(CanHackerReader.INSTANCE.readLine(reader.readLine()));

        CANPacket packet = CanHackerReader.INSTANCE.readLine(reader.readLine());

        assertEquals(0x4A0, packet.getId());
        assertEquals(8, packet.getData().length);
        assertEquals((byte)0xCC, packet.getData()[0]);
    }
}
