package com.rusefi.can.reader.dbc;

import org.junit.Test;

import static com.rusefi.can.reader.dbc.DbcFile.applyOrderForStartOffset;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DbcFieldTest {
    @Test
    public void testBigEndian() {
        {
            applyOrderForStartOffset = true;
            DbcField field = create(true);
            assertFalse(field.coversByte(1));
            assertFalse(field.coversByte(2));
            assertTrue(field.coversByte(3));
            assertFalse(field.coversByte(0));
        }
        {
            applyOrderForStartOffset = false;
            DbcField field = create(true);
            assertTrue(field.coversByte(0));
            assertTrue(field.coversByte(1));
            assertFalse(field.coversByte(3));
        }
        applyOrderForStartOffset = true;
    }

    private static DbcField create(boolean isBigEndian) {
        return new DbcField("", 8, 16, 1, 0, null, isBigEndian);
    }

    @Test
    public void testLittleEndian() {
        DbcField field = create(false);
        assertFalse(field.coversByte(0));
        assertTrue(field.coversByte(1));
        assertTrue(field.coversByte(2));
        assertFalse(field.coversByte(3));
    }
}
