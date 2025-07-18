package com.rusefi.can.reader.dbc;

import com.rusefi.can.CANPacket;
import org.junit.Test;

import static com.rusefi.can.reader.dbc.DbcFile.compatibilityWithBrokenRusEfiLogic;
import static org.junit.Assert.*;

public class DbcFieldTest {
    @Test
    public void testBigEndian() {
        {
            // todo: sorry I do not trust this test :(
            // it looks life coversByte() is completely broken for big endian signals
            compatibilityWithBrokenRusEfiLogic = false;
            DbcField field = create(true, false);
            assertTrue(field.coversByte(0));
            assertFalse(field.coversByte(1));
            assertFalse(field.coversByte(2));
            assertFalse(field.coversByte(3));
        }
        {
            compatibilityWithBrokenRusEfiLogic = true;
            DbcField field = create(true, false);
            assertFalse(field.coversByte(0));
            assertTrue(field.coversByte(1));
            assertFalse(field.coversByte(2));
            assertFalse(field.coversByte(3));
        }
        compatibilityWithBrokenRusEfiLogic = false;
    }

    private static DbcField create(boolean isBigEndian, boolean isSigned) {
        // will use bytes 1 and 2 (in C++ numbering)
        int startBit = isBigEndian ? 15 : 8;
        int length = 16;
        return new DbcField(-1, "", startBit, length, 1, 0, null, isBigEndian, isSigned);
    }

    @Test
    public void testLittleEndian() {
        DbcField field = create(false, false);
        assertFalse(field.coversByte(0));
        assertTrue(field.coversByte(1));
        assertTrue(field.coversByte(2));
        assertFalse(field.coversByte(3));
    }

    @Test
    public void testSigned() {
        DbcField signedField = create(true, true);
        DbcField unsignedField = create(true, false);

        CANPacket pkt1 = new CANPacket(0, -1, new byte[] {0x00, (byte)0x7F, (byte)0xFF});
        CANPacket pkt2 = new CANPacket(0, -1, new byte[] {0x00, (byte)0xFF, (byte)0xFF});

        assertEquals(32767.0, signedField.getValue(pkt1), 0.001);
        assertEquals(-1.0, signedField.getValue(pkt2), 0.001);

        assertEquals(32767.0, unsignedField.getValue(pkt1), 0.001);
        assertEquals(65535.0, unsignedField.getValue(pkt2), 0.001);
    }
}
