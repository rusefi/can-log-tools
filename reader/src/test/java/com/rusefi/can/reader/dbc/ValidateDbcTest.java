package com.rusefi.can.reader.dbc;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ValidateDbcTest {
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
        assertTrue(errors.get(0).contains("REVERSED suffix"));
    }

    @Test
    public void testWrongOrderNoLetters() {
        List<String> errors = ValidateDbc.checkPacket(22, "MSG_16_22"); // 0x16 is 22.
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Wrong order"));
    }
}
