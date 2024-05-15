package com.rusefi.can.reader.dbc;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DbcFieldTest {
    @Test
    public void testBigEndian() {
        DbcField field = new DbcField("", 8, 16, 1, 0, null, true);
        assertFalse(field.coversByte(0));
        assertTrue(field.coversByte(1));
        assertTrue(field.coversByte(2));
        assertFalse(field.coversByte(3));
    }

    @Test
    public void testLittleEndian() {
        DbcField field = new DbcField("", 8, 16, 1, 0, null, false);
        assertFalse(field.coversByte(0));
        assertTrue(field.coversByte(1));
        assertTrue(field.coversByte(2));
        assertFalse(field.coversByte(3));
    }
}
