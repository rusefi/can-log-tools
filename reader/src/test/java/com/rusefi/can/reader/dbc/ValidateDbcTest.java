package com.rusefi.can.reader.dbc;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ValidateDbcTest {
    @Test
    public void testValidDbc() {
        List<String> lines = Arrays.asList(
            "BO_ 1043 MSG_1043_413: 8 Vector__XXX"
        );
        List<String> errors = ValidateDbc.checkDbcLines(lines);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValueMismatch() {
        // dec_suffix=413, hex_suffix=19E. 413 != 0x19E (which is 414).
        List<String> lines = Arrays.asList(
            "BO_ 1043 MSG_413_19E: 8 Vector__XXX"
        );
        List<String> errors = ValidateDbc.checkDbcLines(lines);
        // This will have ID mismatch as well because 413 != 1043.
        assertEquals(2, errors.size());
        assertTrue(errors.get(0).contains("Value mismatch") || errors.get(1).contains("Value mismatch"));
    }

    @Test
    public void testIdMismatch() {
        List<String> lines = Arrays.asList(
            "BO_ 1000 MSG_413_19D: 8 Vector__XXX"
        );
        List<String> errors = ValidateDbc.checkDbcLines(lines);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("ID mismatch"));
    }

    @Test
    public void testReversedSuffix() {
        List<String> lines = Arrays.asList(
            "BO_ 1043 MSG_19D_413: 8 Vector__XXX"
        );
        List<String> errors = ValidateDbc.checkDbcLines(lines);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("REVERSED suffix"));
    }

    @Test
    public void testWrongOrderNoLetters() {
        List<String> lines = Arrays.asList(
            "BO_ 10 MSG_A_10: 8 Vector__XXX" // 0xA is 10
        );
        List<String> errors = ValidateDbc.checkDbcLines(lines);
        // Actually A has letters.
        // Let's use something that is same in both but intended as hex first.
        // Wait, the logic says if int(d_str) == int(h_str, 16).
        // If it's _10_16 (decimal 10, hex 16 which is 22) -> mismatch.
        // If it's _16_10 (hex 16 is 22, decimal 10) -> reversed? No.
        // The script says: rev_match = re.search(r'_([0-9A-F]+)_(\d+)$', msg_name)
        // If it matches h_str and d_str.
        // If int(d_str) == int(h_str, 16) and has letters -> REVERSED.
        // Else if int(d_str) == int(h_str, 16) but no letters -> Wrong order.
        
        lines = Arrays.asList(
            "BO_ 22 MSG_16_22: 8 Vector__XXX" // 0x16 is 22.
        );
        errors = ValidateDbc.checkDbcLines(lines);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Wrong order"));
    }
}
