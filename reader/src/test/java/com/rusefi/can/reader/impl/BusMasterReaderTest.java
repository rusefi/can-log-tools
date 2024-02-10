package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class BusMasterReaderTest {
    @Test
    public void test() throws IOException {
        String line = "21:27:52:3456 Rx 1 0x309 s 7 47 D8 7F D4 A8 00 05 ";
        BufferedReader reader = new BufferedReader(new StringReader(line));
        CANPacket packet = BusMasterReader.INSTANCE.readLine(reader.readLine());
        assertEquals(packet.getData().length, 7);
        assertEquals(packet.getData()[0], 0x47);
        assertEquals(packet.getData()[6], 0x05);
    }
}
