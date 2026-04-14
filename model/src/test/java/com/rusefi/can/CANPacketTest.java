package com.rusefi.can;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CANPacketTest {
    @Test
    public void testToString() {
        byte[] data = new byte[]{0x12, 0x34, (byte) 0xAB, (byte) 0xCD};
        CANPacket packet = new CANPacket(123.45, 0x123, data);
        
        // DualSid.dualSid(0x123) should be "291_123"
        // HexBinary.printHexBinary(data) should be "12 34 AB CD"
        assertEquals("CANPacket{id=291_123, data=12 34 AB CD}", packet.toString());
    }
}
