package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcPacket;
import com.rusefi.can.dbc.reader.DbcFieldParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParseDbcFieldTest {

    private static final double EPS = 0.001;

    @Test
    public void parseIat() {
        String line = "SG_ Ansauglufttemperatur : 8|8@1+ (0.75,-48) [-48|142.5] \"\" XXX";
        DbcField iatField = DbcFieldParser.parseField(line, "hello", -1);
        assertEquals("Ansauglufttemperatur", iatField.getName());
        assertEquals(0.75, iatField.getMult(), EPS);
        assertEquals(-48, iatField.getOffset(), EPS);

        CANPacket packet = new PcanTrcReader1_1().readLine("  2197)      1234.8  Rx         0380  8  00 62 FA 00 22 00 00 FA");
        assertEquals(8, packet.getData().length);

        assertEquals(25.5, iatField.getValue(packet), EPS);
    }

    @Test
    public void testHumanStartIndex() {
        // SG_ CrksftNTrnsRegCmdTq : 51|12@0+ (0.5,-848) [-848|1199.5] "Nm"  TCM_HS
        // Byte order: Motorola (Big Endian) '@0'
        // bit 51 is MSB.
        // Motorola MSB bit 51 is byte 6, bit 3.
        // Bits: 51, 50, 49, 48 (byte 6) | 63, 62, 61, 60, 59, 58, 57, 56 (byte 7)
        // LSB is bit 56 (byte 7, bit 0).
        DbcField field = DbcFieldParser.parseField("SG_ CrksftNTrnsRegCmdTq : 51|12@0+ (0.5,-848) [-848|1199.5] \"Nm\"  TCM_HS", "hello", -1);
        assertEquals(56, field.getLsbBitIndex());   // lsb
        assertEquals(12, field.getLength());

        assertEquals(51, field.getDbcStartIndex()); // msb
    }

    @Test
    public void testMotorolaValue() {
        // SG_ Field : 55|16@0+ (1,0) [0|65535] ""
        // MSB = 55 (byte 6, bit 7)
        // LSB = 56 (byte 7, bit 0)
        // Byte 6 contains upper 8 bits, Byte 7 contains lower 8 bits.
        DbcField field = DbcFieldParser.parseField("SG_ Field : 55|16@0+ (1,0) [0|65535] \"\" XXX", "hello", -1);
        
        // Data: byte 6 = 0x12, byte 7 = 0x34
        byte[] data = new byte[8];
        data[6] = 0x12;
        data[7] = 0x34;
        
        CANPacket packet = new CANPacket(0, 0, data);
        assertEquals(0x1234, field.getRawValue(packet));
    }

    @Test
    public void testMotorolaValue3Bytes() {
        // MSB = 7 (byte 0, bit 7)
        // Length = 24
        // Bytes 0 (MSB), 1, 2 (LSB)
        DbcField field = DbcFieldParser.parseField("SG_ Field : 7|24@0+ (1,0) [0|16777215] \"\" XXX", "hello", -1);
        
        byte[] data = new byte[8];
        data[0] = 0x12;
        data[1] = 0x34;
        data[2] = 0x56;
        
        CANPacket packet = new CANPacket(0, 0, data);
        assertEquals(0x123456, field.getRawValue(packet));
    }

    @Test
    public void testMotorolaUnaligned() {
        // MSB = 12 (byte 1, bit 4)
        // Length = 12
        // Bits: 12, 11, 10, 9, 8 (byte 1) | 23, 22, 21, 20, 19, 18, 17, 16 (byte 2) NO
        // Actually:
        // Byte 1: MSB is bit 12. Bits in byte 1 are 15..8. Signal uses 12..8 (5 bits)
        // Byte 2: Remaining 7 bits: 23..17.
        // MSB0 mapping: bit 12 (LSB0) -> Byte 1, bit 4.
        // signal starts at bit 12 (MSB), length 12.
        // In Motorola, it goes 'backwards' in bits but forwards in bytes if needed?
        // No, Motorola magic:
        // 12 -> 11 (msb0)
        // 11 + 12 - 1 = 22
        // 22 -> 21 (lsb0) = LSB.
        // MSB=12 (byte 1, bit 4), LSB=21 (byte 2, bit 5)
        DbcField field = DbcFieldParser.parseField("SG_ Field : 12|12@0+ (1,0) [0|4095] \"\" XXX", "hello", -1);

        byte[] data = new byte[8];
        // 0xABC = 1010 1011 1100
        // MSB (5 bits) = 10101 = 0x15. In byte 1 (15..8), these are bits 12..8.
        // Byte 1: x x x 1 0 1 0 1  -> 0x15
        // LSB (7 bits) = 0111100 = 0x3C. In byte 2 (23..16), these are bits 23..17.
        // Byte 2: 0 1 1 1 1 0 0 x  -> 0x78 (if bit 16 is 0)
        data[1] = 0x15;
        data[2] = 0x78;

        CANPacket packet = new CANPacket(0, 0, data);
        assertEquals(0xABC, field.getRawValue(packet));
    }

    @Test
    public void testLargeMotorolaAtEnd() {
        // SG_ Field : 7|32@0+ (1,0) [0|4294967295] "" XXX
        // MSB = 7 (byte 0, bit 7)
        // Length = 32
        DbcField field = DbcFieldParser.parseField("SG_ Field : 7|32@0+ (1,0) [0|4294967295] \"\" XXX", "hello", -1);
        
        // Let's see what it actually is
        // System.out.println("[DEBUG_LOG] startOffset=" + field.getStartOffset());

        byte[] data = new byte[8];
        data[0] = 0x12;
        data[1] = 0x34;
        data[2] = 0x56;
        data[3] = 0x78;
        CANPacket packet = new CANPacket(0, 0, data);
        long val = field.getRawValue(packet) & 0xFFFFFFFFL;
        // System.out.println("[DEBUG_LOG] val=" + val);

        // This should pass validation in DbcPacket for an 8-byte message
        new DbcPacket(1, "Test", "src", 8, java.util.Arrays.asList(field), null);
    }
    @Test(expected = IllegalStateException.class)
    public void testBigEndianOutOfBounds() {
        // MSB = 0, length = 16 -> should use bytes 0 and -1 (invalid)
        // 0 -> 7 (msb0)
        // 7 + 16 - 1 = 22 (msb0)
        // 22 -> 21 (lsb0) = startOffset
        // lsbByte = 2, shift = 5. numBytes = (5 + 16 + 7) / 8 = 3. msbByte = 2 - 3 + 1 = 0. PASSES?
        // Wait, if MSB=0 (byte 0, bit 0), and length=16. 
        // It uses bits 0, 15, 14, 13, 12, 11, 10, 9, 8, 23, 22, 21, 20, 19, 18, 17?
        // NO, Motorola: MSB=0 is byte 0, bit 0. Next bits are byte 1, 2...
        // Motorola goes 'forward' in bytes.
        // MSB=0, length 16 -> Byte 0 (bits 0..7) and Byte 1 (bits 15..8).
        // This is perfectly valid!
        
        // Let's find something TRULY out of bounds.
        // How about length=100 in 8 byte packet?
        DbcField field = DbcFieldParser.parseField("SG_ Field : 7|100@0+ (1,0) [0|65535] \"\" XXX", "hello", -1);
        new DbcPacket(1, "Test", "src", 8, java.util.Arrays.asList(field), null);
    }

    @Test(expected = IllegalStateException.class)
    public void testBigEndianOutOfBoundsHigh() {
        // MSB = 63, length = 16 -> LSB is 64? (invalid)
        // 63 is byte 7, bit 7.
        // 16 bits -> bytes 7 and 8?
        // Wait, 63 -> 56 (msb0)
        // 56 + 16 - 1 = 71 (msb0)
        // 71 -> 64 (lsb0)
        DbcField field = DbcFieldParser.parseField("SG_ Field : 63|16@0+ (1,0) [0|65535] \"\" XXX", "hello", -1);
        new DbcPacket(1, "Test", "src", 8, java.util.Arrays.asList(field), null);
    }
}
