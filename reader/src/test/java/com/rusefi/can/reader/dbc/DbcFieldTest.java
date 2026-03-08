package com.rusefi.can.reader.dbc;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcField;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.*;

public class DbcFieldTest {
    @Test
    public void testBigEndian() {
        {
            // todo: sorry I do not trust this test :(
            // it looks life coversByte() is completely broken for big endian signals
            DbcField field = create(true, false);
            assertTrue(field.coversByte(0));
            assertFalse(field.coversByte(1));
            assertFalse(field.coversByte(2));
            assertFalse(field.coversByte(3));
        }
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

    @Test
    public void testShortName() {
        DbcField field = new DbcField(-1, "OriginalName", 0, 8, 1, 0, null, false, false);
        assertEquals("OriginalName", field.getName());
        assertEquals("OriginalName", field.getShortName());

        field.rename("Longer Nice Name");
        assertEquals("Longer Nice Name", field.getName());
        assertEquals("OriginalName", field.getShortName());
    }

    private static DbcField makeField(int startBit, int length, boolean isBigEndian) {
        return new DbcField(-1, "", startBit, length, 1, 0, null, isBigEndian, false);
    }

    @Test
    public void testUsedBitsIntel() {
        DbcField field1 = makeField(4, 10, false);
        BitSet usedBits = new BitSet(8*8);
        field1.getUsedBits(usedBits);
        // used 4..7, 8..13
        assertEquals(10, usedBits.cardinality());
        assertEquals(4, usedBits.nextSetBit(0));
        assertEquals(14, usedBits.nextClearBit(4));

        DbcField field2 = makeField(16, 32, false);
        usedBits.clear();
        field2.getUsedBits(usedBits);
        // used 16..47
        assertEquals(32, usedBits.cardinality());
        assertEquals(16, usedBits.nextSetBit(0));
        assertEquals(48, usedBits.nextClearBit(16));

        DbcField field3 = makeField(18, 32, false);
        usedBits.clear();
        field3.getUsedBits(usedBits);
        // used 18..49
        assertEquals(32, usedBits.cardinality());
        assertEquals(18, usedBits.nextSetBit(0));
        assertEquals(50, usedBits.nextClearBit(18));

    }

    @Test
    public void testUsedBitsMotorola() {
        DbcField field1 = makeField(3, 10, true);
        BitSet usedBits = new BitSet(8*8);
        field1.getUsedBits(usedBits);
        // byte 0: _ _ _ _ x x x x  bits 3..0
        // byte 1: x x x x x x _ _  bits 15..10
        assertEquals(10, usedBits.cardinality());
        assertEquals(4, usedBits.nextClearBit(0));
        assertEquals(10, usedBits.nextSetBit(4));
        assertEquals(16, usedBits.nextClearBit(10));

        DbcField field2 = makeField(23, 32, true);
        usedBits.clear();
        field2.getUsedBits(usedBits);
        // used 16..47 (bytes 2 3 4 5)
        assertEquals(32, usedBits.cardinality());
        assertEquals(16, usedBits.nextSetBit(0));
        assertEquals(48, usedBits.nextClearBit(16));

        DbcField field3 = makeField(18, 32, true);
        usedBits.clear();
        field3.getUsedBits(usedBits);
        // byte 2: _ _ _ _ _ x x x  bits 18..16
        // byte 3: x x x x x x x x  bits 31..24
        // byte 4: x x x x x x x x  bits 39..32
        // byte 5: x x x x x x x x  bits 47..40
        // byte 6: x x x x x _ _ _  bits 55..51
        assertEquals(32, usedBits.cardinality());
        assertEquals(16, usedBits.nextSetBit(0));
        assertEquals(19, usedBits.nextClearBit(16));
        assertEquals(24, usedBits.nextSetBit(19));
        assertEquals(48, usedBits.nextClearBit(24));
        assertEquals(51, usedBits.nextSetBit(48));
        assertEquals(56, usedBits.nextClearBit(51));
    }
}
