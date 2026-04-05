package com.rusefi.can.reader.dbc;

import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcPacket;
import com.rusefi.can.tool.ValidateDbc;
import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import static org.junit.Assert.*;

public class ValidateDbcTest {
    @Test
    public void testOverlap() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "F2", 7, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertFalse("Should have errors", errors.isEmpty());
        assertTrue(errors.get(0).contains("Overlap"));
        assertTrue(errors.get(0).contains("uses bit 7"));
    }

    @Test
    public void testNoOverlap() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "F2", 8, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertTrue("Should not have errors", errors.isEmpty());
    }

    @Test
    public void testGapIgnored() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "some_gap_8", 0, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertTrue("Should not have errors because gap is ignored", errors.isEmpty());
    }

    @Test
    public void testValidDbc() {
        List<String> errors = ValidateDbc.checkPacket(1043, "MSG_1043_413");
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValueMismatch() {
        // dec_suffix=413, hex_suffix=19E. 413 != 0x19E (which is 414).
        List<String> errors = ValidateDbc.checkPacket(1043, "MSG_413_19E");
        // This will have ID mismatch as well because 413 != 1043.
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Value mismatch")));
    }

    @Test
    public void testIdMismatch() {
        List<String> errors = ValidateDbc.checkPacket(1000, "MSG_413_19D");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("ID mismatch"));
    }

    @Test
    public void testReversedSuffix() {
        List<String> errors = ValidateDbc.checkPacket(1043, "MSG_19D_413");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("REVERSED suffix: MSG_19D_413 should be MSG_413_19D"));
    }

    @Test
    public void testWrongOrderNoLetters() {
        List<String> errors = ValidateDbc.checkPacket(22, "MSG_16_22"); // 0x16 is 22.
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Wrong order: MSG_16_22 (hex before decimal) should be MSG_22_16"));
    }

    @Test
    public void testBigEndian() {
        // SG_ PrplsnTrqRelCap : 63|8@0+ (0.5,0) [0|127.5] "%" TCM_HS
        DbcField field1 = new DbcField(1417, "PrplsnTrqRelCap", 63, 8, 0.5, 0, "", true, false);
        // SG_ ETC_MinTorq : 47|12@0+ (0.5,-848) [-848|1199.5] "Nm" TCM_HS,HCP_HS,BCP_HS
        DbcField field2 = new DbcField(1417, "ETC_MinTorq", 47, 12, 0.5, -848, "", true, false);
        // SG_ ETC_MinRunTorq : 27|12@0+ (0.5,-848) [-848|1199.5] "Nm" TCM_HS,HCP_HS,BCP_HS
        DbcField field3 = new DbcField(1417, "ETC_MinRunTorq", 27, 12, 0.5, -848, "", true, false);

        DbcPacket packet = new DbcPacket(1417, "ETEI_Engine_Torque_Capability_1417_589", "src", 8,
                Arrays.asList(field1, field2, field3), null);

        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertTrue("Should not have errors: " + errors, errors.isEmpty());
    }

    @Test
    public void testBigEndianFull() {
        // SG_ PrplsnTrqRelCap : 63|8@0+ (0.5,0) [0|127.5] "%" TCM_HS
        DbcField field1 = new DbcField(1417, "PrplsnTrqRelCap", 63, 8, 0.5, 0, "", true, false);
        // SG_ ETC_MinTorq : 47|12@0+ (0.5,-848) [-848|1199.5] "Nm" TCM_HS,HCP_HS,BCP_HS
        DbcField field2 = new DbcField(1417, "ETC_MinTorq", 47, 12, 0.5, -848, "", true, false);
        // SG_ ETC_MinRunTorq : 27|12@0+ (0.5,-848) [-848|1199.5] "Nm" TCM_HS,HCP_HS,BCP_HS
        DbcField field3 = new DbcField(1417, "ETC_MinRunTorq", 27, 12, 0.5, -848, "", true, false);
        // SG_ ETC_MaxTorq : 23|12@0+ (0.5,-848) [-848|1199.5] "Nm" TCM_HS,HCP_HS,BCP_HS
        DbcField field4 = new DbcField(1417, "ETC_MaxTorq", 23, 12, 0.5, -848, "", true, false);
        // SG_ ETC_RefEngSpd : 15|8@0+ (32,0) [0|8160] "rpm" TCM_HS,HCP_HS,BCP_HS
        DbcField field5 = new DbcField(1417, "ETC_RefEngSpd", 15, 8, 32, 0, "", true, false);
        // SG_ ETC_UnsdRsrvd : 2|3@0+ (1,0) [0|7] "" TCM_HS,HCP_HS,BCP_HS
        DbcField field6 = new DbcField(1417, "ETC_UnsdRsrvd", 2, 3, 1, 0, "", true, false);
        // SG_ ETC_FrmCntr : 6|4@0+ (1,0) [0|15] "" TCM_HS,HCP_HS,BCP_HS
        DbcField field7 = new DbcField(1417, "ETC_FrmCntr", 6, 4, 1, 0, "", true, false);
        // SG_ ETC_EngOpMd : 7|1@0+ (1,0) [0|1] "" TCM_HS,HCP_HS,BCP_HS
        DbcField field8 = new DbcField(1417, "ETC_EngOpMd", 7, 1, 1, 0, "", true, false);

        DbcPacket packet = new DbcPacket(1417, "ETEI_Engine_Torque_Capability_1417_589", "src", 8,
                Arrays.asList(field1, field2, field3, field4, field5, field6, field7, field8), null);

        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertTrue("Should not have errors: " + errors, errors.isEmpty());
    }

    @Test
    public void test2byteMotorolaByteRange() {
        // SG_ WRSRDWhlDistTmstm : 55|16@0+ (1,0) [0|65535] "counts"
        DbcField shouldFitWithin8Bytes = new DbcField(193, "WRSRDWhlDistTmstm", 55, 16, 1, 0, "", true, false);

        DbcPacket packet = new DbcPacket(193, "PPEI_DrvnWheelRotationalSt_193_0C1", "src", 8,
                Arrays.asList(shouldFitWithin8Bytes), null);

        BitSet usedBits = shouldFitWithin8Bytes.getUsedBits();
        assertEquals(64, usedBits.size());
        for (int i = 0; i < 48; i++) assertFalse(usedBits.get(i));
        for (int i = 48; i < 64; i++) assertTrue(usedBits.get(i));

    }

    @Test
    public void testSameEndianness() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "F2", 8, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsEndianness(packet);
        assertTrue("Should not have errors", errors.isEmpty());
    }

    @Test
    public void testMixedEndianness() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "F2", 8, 8, 1, 0, "", true, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsEndianness(packet);
        assertFalse("Should have errors", errors.isEmpty());
        assertTrue(errors.get(0).contains("Mixed endianness"));
    }

    @Test
    public void testMixedEndiannessWithGap() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "some_gap_8", 8, 8, 1, 0, "", true, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsEndianness(packet);
        assertTrue("Should not have errors because gap is ignored", errors.isEmpty());
    }

    @Test
    public void testGapFirstField() {
        DbcField field1 = new DbcField(100, "some_gap_8", 0, 8, 1, 0, "", true, false);
        DbcField field2 = new DbcField(100, "F1", 8, 8, 1, 0, "", false, false);
        DbcField field3 = new DbcField(100, "F2", 16, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", 8, Arrays.asList(field1, field2, field3), null);
        List<String> errors = ValidateDbc.checkFieldsEndianness(packet);
        assertTrue("Should not have errors because gap is ignored and F1, F2 match", errors.isEmpty());
    }
}
