package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class SomethingLinuxReaderTest {
    @Test
    public void test() throws IOException {
        String line = "(1666123717.446553) can0 215#09344FA34F8E01";
        BufferedReader reader = new BufferedReader(new StringReader(line));
        CANPacket packet = SomethingLinuxReader.INSTANCE.readLine(reader.readLine());
        assertEquals(packet.getData().length, 7);
        assertEquals(packet.getData()[0], 0x09);
        assertEquals(packet.getData()[6], 0x01);
    }
}
