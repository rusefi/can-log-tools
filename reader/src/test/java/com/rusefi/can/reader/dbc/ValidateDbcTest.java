package com.rusefi.can.reader.dbc;

import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcPacket;
import com.rusefi.can.tool.ValidateDbc;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ValidateDbcTest {
    @Test
    public void testOverlap() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "F2", 7, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertFalse("Should have errors", errors.isEmpty());
        assertTrue(errors.get(0).contains("Overlap"));
        assertTrue(errors.get(0).contains("uses bit 7"));
    }

    @Test
    public void testNoOverlap() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "F2", 8, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", Arrays.asList(field1, field2), null);
        List<String> errors = ValidateDbc.checkFieldsOverlap(packet);
        assertTrue("Should not have errors", errors.isEmpty());
    }

    @Test
    public void testGapIgnored() {
        DbcField field1 = new DbcField(100, "F1", 0, 8, 1, 0, "", false, false);
        DbcField field2 = new DbcField(100, "some_gap_8", 0, 8, 1, 0, "", false, false);

        DbcPacket packet = new DbcPacket(100, "MSG", "src", Arrays.asList(field1, field2), null);
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
}
