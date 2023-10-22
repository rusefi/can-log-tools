package com.rusefi.can.analysis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Crc8Test {
    @Test
    public void test() {
        J1850_SAE_crc8_Calculator c = new J1850_SAE_crc8_Calculator();

        assertEquals((byte) 0xfc, c.crc8(new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0xa0, 0x00, 0x2d}, 7));
    }
}
